package cz.mzk.recordmanager.server.scripting.marc;

import com.google.common.primitives.Chars;
import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.view.ViewType;
import cz.mzk.recordmanager.server.model.Ean;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.scripting.BaseDSL;
import cz.mzk.recordmanager.server.scripting.ListResolver;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;
import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import cz.mzk.recordmanager.server.util.identifier.ISSNUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MarcDSL extends BaseDSL {

	private MetadataRecord metadataRecord;

	private final static String EMPTY_SEPARATOR = "";
	private final static String SPACE_SEPARATOR = " ";

	private final static String MAP_CATEGORY_SUBCATEGORY = "conspectus_category_subcategory.map";
	private final static String MAP_SUBCATEGORY_NAME = "conspectus_subcategory_name.map";
	private final static String MAP_CONSPECTUS_NAMES = "conspectus_names.map";
	private final static String MAP_CONSPECTUS_CATEGORY = "conspectus_category.map";
	private final static String MAP_CONSPECTUS_SUBCAT_CAT_CHANGE = "conspectus_category_change.map";
	private final static String MAP_FORMAT_SEARCH = "format_search.map";
	private static final String MAP_PSH = "psh.map";

	private static final Pattern FIELD_PATTERN = Pattern.compile("([0-9]{3})([a-zA-Z0-9]*)");
	private static final Pattern NON_WORD_PATTERN = Pattern.compile("\\W");
	private static final Pattern TITLE_END_PUNCTUATION = Pattern.compile("[:,=;/.]+$");
	private static final Pattern TITLE_LEAD_SPACE = Pattern.compile("^ *");
	private static final Pattern TITLE_PACK_SPACES = Pattern.compile(" +");
	private static final Pattern TITLE_NUMBERS = Pattern.compile("([0-9])[.,]([0-9])");
	private static final Pattern TITLE_SUPPRESS = Pattern.compile("<<[^<{2}]*>>");
	private static final Pattern TITLE_TO_BLANK = Pattern.compile("['\\[\\]\"`!()\\-{};:.,?/@*%=^_|~]");
	private static final Pattern PATTERN_653A = Pattern.compile("forma:.*|nosič:.*|způsob vydávání:.*|úroveň zpracování:.*");
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");
	private static final Pattern SPLIT_COLON = Pattern.compile(":");
	private static final Pattern SPLIT_SEMICOLON = Pattern.compile(";");
	private static final Pattern SPLIT_DOT = Pattern.compile("\\.");
	private static final Pattern SPLIT_VERTICAL_BAR = Pattern.compile("\\|");
	private static final Pattern Z_CYKLU_787 = Pattern.compile("^z cyklu", Pattern.CASE_INSENSITIVE);

	private static final String LINK773_ISBN = "isbn:%s";
	private static final String LINK773_ISSN = "issn:";
	private static final String LINK773_ISMN = "ismn:";
	private static final String LINK773_TITLE = "title:";

	private static final String DISPLAY773_ISMN = "ISMN ";
	private static final String DISPLAY773_ISSN = "ISSN ";
	private static final String DISPLAY773_ISBN = "ISBN ";
	private static final String DISPLAY773_JOINER = ". -- ";

	private final MarcFunctionContext context;

	private final MarcRecord record;

	private final Map<String, RecordFunction<MarcFunctionContext>> functions;

	public MarcDSL(MarcFunctionContext context, MappingResolver propertyResolver, StopWordsResolver stopWordsResolver,
				   ListResolver listResolver, Map<String, RecordFunction<MarcFunctionContext>> functions) {
		super(propertyResolver, stopWordsResolver, listResolver);
		this.context = context;
		this.record = context.record();
		this.functions = functions;
		this.metadataRecord = context.metadataRecord();
	}

	public MarcRecord getRecord() {
		return record;
	}

	public String getFirstField(String tags) {
		for (String tag : SPLIT_COLON.split(tags)) {
			Matcher matcher = FIELD_PATTERN.matcher(tag);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Tag can't be parsed: " + tag);
			}
			String fieldTag = matcher.group(1);
			String subFields = matcher.group(2);
			String result = record.getField(fieldTag, subFields.toCharArray());
			if (result != null) return result;
		}
		return null;
	}

	public String getControlField(String tag) {
		return record.getControlField(tag);
	}

	public List<String> getFields(String tags) {
		return this.getFields(tags, SubfieldExtractionMethod.JOINED);
	}

	public List<String> getFields(String tags, SubfieldExtractionMethod method) {
		List<String> result = new ArrayList<>();
		for (String tag : SPLIT_COLON.split(tags)) {
			Matcher matcher = FIELD_PATTERN.matcher(tag);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Tag can't be parsed: "
						+ tag);
			}
			String fieldTag = matcher.group(1);
			String subFields = matcher.group(2);
			result.addAll(record.getFields(fieldTag, null, method, " ",
					subFields.toCharArray()));
		}
		return result;
	}

	public Set<String> getLanguages() {
		Set<String> languages = new HashSet<>();
		String f008 = record.getControlField("008");
		if (f008 != null && f008.length() >= 38) {
			languages.add(f008.substring(35, 38));
		}
		languages.addAll(getFields("041ade", SubfieldExtractionMethod.SEPARATED));

		Set<String> results = new HashSet<>();
		for (String language : languages) {
			language = language.toLowerCase();
			language = CleaningUtils.replaceAll(language, NON_WORD_PATTERN, "");
			results.add(language);
		}
		return results;
	}

	public String getCountry() {
		String f008 = record.getControlField("008");
		if (f008 != null && f008.length() > 18) {
			return f008.substring(15, 18).trim();
		}
		String s = getFirstField("044a");
		if (s != null) return getFirstField("044a").trim();

		return "";
	}

	public Set<String> getCountries() {
		Set<String> countries = new HashSet<>();
		String f008 = record.getControlField("008");
		if (f008 != null && f008.length() > 18) {
			countries.add(f008.substring(15, 18).trim());
		}
		countries.addAll(getFields("044a", SubfieldExtractionMethod.SEPARATED).stream().map(String::trim).collect(Collectors.toSet()));
		return countries;
	}

	/**
	 * Get all fields starting with the 100 and ending with the 839
	 * This will ignore any "code" fields and only use textual fields
	 */
	public List<String> getAllFields() {
		Map<String, List<DataField>> allFields = record.getAllFields();
		List<String> results = new ArrayList<>();
		for (Entry<String, List<DataField>> entry : allFields.entrySet()) {
			int tag;
			try {
				tag = Integer.parseInt(entry.getKey());
			} catch (NumberFormatException nfe) {
				continue;
			}
			if ((tag < 100) || (tag >= 840)) {
				continue;
			}
			results.add(SolrUtils.getAllFieldsString(entry.getValue()));
		}
		return results;
	}

	/**
	 * Get the title (245ab) from a record, without non-filing chars as
	 * specified in 245 2nd indicator, and lowercased.
	 *
	 * @return 245a and 245b values concatenated, with trailing punct removed,
	 * and with non-filing characters omitted. Null returned if no
	 * title can be found.
	 */
	public String getSortableTitle() {
		List<DataField> titleFields = record.getAllFields().get("245");
		if (titleFields == null || titleFields.isEmpty()) {
			return "";
		}
		DataField titleField = titleFields.get(0);
		if (titleField == null) return "";

		int nonFilingInt = SolrUtils.getInd2AsInt(titleField);

		List<Title> titles = metadataRecord.getTitle();
		if (titles == null || titles.isEmpty()) return null;
		String title = metadataRecord.getTitle().get(0).getTitleStr();
		title = CleaningUtils.replaceAll(title, TITLE_END_PUNCTUATION, EMPTY_SEPARATOR);
		title = CleaningUtils.replaceAll(title, TITLE_NUMBERS, "$1$2");
		title = title.toLowerCase();

		//Skip non-filing chars, if possible.
		if (title.length() > nonFilingInt) {
			title = title.substring(nonFilingInt);
		}

		if (title.isEmpty()) return null;

		title = CleaningUtils.replaceAll(title, TITLE_SUPPRESS, EMPTY_SEPARATOR);
		title = CleaningUtils.replaceAll(title, TITLE_TO_BLANK, SPACE_SEPARATOR);
		title = CleaningUtils.replaceAll(title, TITLE_LEAD_SPACE, EMPTY_SEPARATOR);
		title = CleaningUtils.replaceAll(title, TITLE_PACK_SPACES, SPACE_SEPARATOR);
		return title.trim();
	}

	public String getFullrecord() {
		return metadataRecord.export(IOFormat.ISO_2709);
	}

	public String getFullRecordAsXML() {
		return metadataRecord.export(IOFormat.XML_MARC);
	}

	public Object methodMissing(String methodName, Object args) {
		RecordFunction<MarcFunctionContext> func = functions.get(methodName);
		if (func == null) {
			throw new IllegalArgumentException(String.format("missing function: %s", methodName));
		}
		return func.apply(context, args);
	}

	public List<String> getPublisherStrMv() throws IOException {
		Set<String> publishers = new HashSet<>();
		for (DataField dataField : record.getDataFields("264")) {
			if (dataField.getIndicator2() == '1') {
				dataField.getSubfields('b').forEach(sf -> publishers.add(SolrUtils.cleanPublisherName(sf.getData())));
			}
		}
		for (DataField dataField : record.getDataFields("928")) {
			if (dataField.getIndicator1() == '9') {
				dataField.getSubfields('a').forEach(sf -> publishers.add(SolrUtils.cleanPublisherName(sf.getData())));
			}
		}
		getFields("260b:978ab").forEach(str -> publishers.add(SolrUtils.cleanPublisherName(str)));
		Set<String> result = new HashSet<>();
		for (String publisher : publishers) {
			List<String> newPublisher = translate("publisher.map", publisher, null);
			if (newPublisher == null) result.add(publisher);
			else result.addAll(newPublisher);
		}
		return new ArrayList<>(result);
	}

	public List<String> getPublisher() {
		List<String> publishers = new ArrayList<>();
		for (DataField dataField : record.getDataFields("264")) {
			if (dataField.getIndicator2() == '1') {
				publishers.addAll(getFieldsTrim("264b"));
			}
		}
		publishers.addAll(getFieldsTrim("260b"));

		return publishers;
	}

	public List<String> getPublisherLocal() {
		List<String> results = new ArrayList<>();
		results.addAll(getFields("260b"));
		if (results.isEmpty()) results.addAll(getFields("264b"));
		return results;
	}

	public Set<String> getFieldsTrim(String tags) {
		Set<String> result = new HashSet<>();
		for (String data : getFields(tags)) {
			result.add(SolrUtils.removeEndPunctuation(data));
		}
		return result;
	}

	public Set<String> getFieldsUnique(String tags) {
		return this.getFieldsUnique(tags, SubfieldExtractionMethod.JOINED);
	}

	public Set<String> getFieldsUnique(String tags, SubfieldExtractionMethod method) {
		Set<String> result = new HashSet<>();
		result.addAll(getFields(tags, method));
		return result;
	}

	public String getFirstFieldTrim(String tags) {
		return SolrUtils.removeEndPunctuation(getFirstField(tags));
	}

	public Set<String> getSubject(String tags) throws IOException {
		if (!metadataRecord.subjectFacet()) return Collections.emptySet();
		Set<String> subjects = new HashSet<>();

		for (String subject : getFields(tags, SubfieldExtractionMethod.SEPARATED)) {
			subjects.add(SolrUtils.toUpperCaseFirstChar(subject));
		}

		for (DataField df : record.getDataFields("653")) {
			for (Subfield sf : df.getSubfields('a')) {
				if (!PATTERN_653A.matcher(sf.getData()).matches())
					subjects.add(SolrUtils.toUpperCaseFirstChar(sf.getData()));
			}
		}

		for (DataField df : record.getDataFields("650")) {
			if (df.getSubfield('2') != null && df.getSubfield('2').getData().contains("psh")) {
				if (df.getSubfield('x') != null) {
					subjects.addAll(SolrUtils.toUpperCaseFirstChar(translate(MAP_PSH, df.getSubfield('x').getData(), null)));
				}
			}
		}

		if (metadataRecord.filterSubjectFacet() != null) {
			subjects = new HashSet<>(filter(metadataRecord.filterSubjectFacet(), new ArrayList<>(subjects)));
		}

		return SolrUtils.removeEndParentheses(subjects);
	}

	public Set<String> getISBNISSNISMN() {
		Set<String> result = new HashSet<>();

		for (DataField df : record.getDataFields("024")) {
			if (df.getIndicator1() == '2') {
				result.addAll(getFields("024az"));
			}
		}
		result.addAll(getFields("020az:022az:787xz:902a"));

		return result;
	}

	public Set<String> getTitleSeries() {
		Set<String> result = new HashSet<>();
		result.addAll(getFieldsTrim("130adfgklnpst7:210a:222ab:240adklmprs:242ap:245abnp:246anp:247afp:"
				+ "440a:490anp:700klmnoprst7:710klmnoprst7:711klmnoprst7:730adklmprs7:740anp:765ts9:"
				+ "773kt:780st:785st:787st:800klmnoprst7:810klmnoprst7:811klmnoprst7:830aklmnoprst7"));
		for (DataField df : record.getDataFields("505")) {
			if (df.getIndicator2() == '0') {
				result.addAll(getFieldsTrim("505t"));
			}
		}

		return result;
	}

	public List<String> getHoldings996() {
		List<String> result = new ArrayList<>();
		Map<String, List<DataField>> allFields = record.getAllFields();

		List<DataField> list996 = allFields.get("996");
		if (list996 == null) {
			return result;
		}
		for (DataField dataField : list996) {
			StringBuilder currentSb = new StringBuilder();
			// 996 with '0' in subfield 'q'
			if (dataField.getSubfield('q') != null && dataField.getSubfield('q').getData().equals("0")) {
				continue;
			}
			for (Subfield subfield : dataField.getSubfields()) {
				currentSb.append('$');
				currentSb.append(subfield.getCode());
				currentSb.append(subfield.getData());
			}
			result.add(currentSb.toString());
		}
		return result;
	}

	public List<String> getUrls() {
		return metadataRecord.getUrls();
	}

	public List<String> getSfxIds() {
		List<String> result = new ArrayList<>();
		for (DataField df : record.getDataFields("866")) {
			String subS = "", subX = "";
			if (df.getSubfield('s') != null) {
				subS = df.getSubfield('s').getData();
			}
			if (df.getSubfield('x') != null) {
				subX = df.getSubfield('x').getData();
			}
			if (!subS.isEmpty()) {
				result.add(subS + '|' + subX);
			}
		}
		return result;
	}

	public Long getLoanRelevance() {
		Long count = 0L;
		for (DataField df : record.getDataFields("996")) {
			if (df.getSubfield('n') != null)
				try {
					count += Long.valueOf(df.getSubfield('n').getData());
				} catch (NumberFormatException ignored) {
				}
		}
		return count;
	}

	public String getAuthorForSorting() {
		List<String> authors = getFields("100abcd:110abcd:111abcd:700abcd:710abcd:711abcd");
		if (authors == null || authors.isEmpty()) return null;
		String author = authors.get(0);
		author = author.toLowerCase();
		author = CleaningUtils.replaceAll(author, TITLE_END_PUNCTUATION, EMPTY_SEPARATOR);
		author = CleaningUtils.replaceAll(author, TITLE_SUPPRESS, EMPTY_SEPARATOR);
		author = CleaningUtils.replaceAll(author, TITLE_TO_BLANK, SPACE_SEPARATOR);
		author = CleaningUtils.replaceAll(author, TITLE_LEAD_SPACE, EMPTY_SEPARATOR);
		author = CleaningUtils.replaceAll(author, TITLE_PACK_SPACES, SPACE_SEPARATOR);
		if (author.isEmpty()) return null;
		return author;
	}

	public String getCitationRecordType() {
		return metadataRecord.getCitationFormat().getCitationType();
	}

	public String getTitleDisplay() {
		DataField df = getFirstDataField("245");
		if (df == null) return null;

		final char titleSubfields[] = {'a', 'b', 'n', 'p'};
		final char sfhPunctuation[] = {'.', ',', ':'};
		char endCharH = ' ';
		StringBuilder sb = new StringBuilder();

		for (Subfield sf : df.getSubfields()) {
			// get last punctuation from 'h'
			if (sf.getCode() == 'h') {
				String data = sf.getData().trim();
				if (!data.isEmpty()) {
					if (Chars.contains(sfhPunctuation, data.charAt(data.length() - 1))) {
						endCharH = data.charAt(data.length() - 1);
					}
				}
			} else if (Chars.contains(titleSubfields, sf.getCode())) {
				// print punctuation from h
				if (endCharH != ' ') {
					sb.append(endCharH);
					sb.append(' ');
					endCharH = ' ';
				}
				sb.append(sf.getData());
				sb.append(' ');
			} else endCharH = ' ';
		}
		return SolrUtils.removeEndPunctuation(sb.toString());
	}

	public DataField getFirstDataField(String tag) {
		List<DataField> list = record.getDataFields(tag);
		if (list.isEmpty()) return null;
		else return list.get(0);
	}

	public String getAuthorDisplay() {
		List<DataField> list = record.getDataFields("100");
		if (list.isEmpty()) return null;
		DataField df = list.get(0);
		String name = SolrUtils.getNameForDisplay(df);
		if (name != null && name.isEmpty()) return null;
		else return name;
	}

	public List<String> getAuthor2Display() {
		List<String> result = new ArrayList<>();
		for (DataField df : record.getDataFields("700")) {
			result.add(SolrUtils.getNameForDisplay(df));
		}
		result.addAll(getFields("110ab:111ab:710ab:711ab"));
		return result;
	}

	public String getAuthorAuthorityDisplay() {
		return record.getField("100", '7');
	}

	public String getAuthorExact() {
		List<DataField> list = record.getDataFields("100");
		if (list.isEmpty()) return null;
		DataField df = list.get(0);
		String name = SolrUtils.getNameForExact(df);
		if (name != null && name.isEmpty()) return null;
		else return name;
	}

	public List<String> getAuthorFind() {
		List<String> result = new ArrayList<>();
		result.add(getAuthorDisplay());
		result.addAll(getAuthor2Display());
		return result;
	}

	public Set<String> getAuthorityIds(String tags) {
		Set<String> result = new HashSet<>();
		for (String tag : SPLIT_VERTICAL_BAR.split(tags)) {
			String[] split = SPLIT_DOT.split(tag);
			for (String value : SPLIT_COLON.split(split[1])) {
				Matcher matcher = FIELD_PATTERN.matcher(value);
				if (!matcher.matches()) {
					throw new IllegalArgumentException("Tag can't be parsed: "
							+ value);
				}
				String fieldTag = matcher.group(1);
				String subFields = matcher.group(2);
				switch (split[0]) {
				case Constants.PREFIX_AUTH:
					record.getFields(fieldTag, " ", subFields.toCharArray())
							.forEach(s -> result.add(SolrUtils.getVizFieldCode(split[0], fieldTag, s)));
					break;
				case Constants.PREFIX_MESH:
					result.addAll(getTezauruForVizField(fieldTag, split[0], "czmesh"));
					break;
				case Constants.PREFIX_AGROVOC:
					result.addAll(getTezauruForVizField(fieldTag, split[0], "agrovoc"));
					break;
				default:
					break;
				}
			}
		}
		return result;
	}

	private Set<String> getTezauruForVizField(String fieldTag, String source, String subfield2) {
		Set<String> results = new HashSet<>();
		for (DataField df : record.getDataFields(fieldTag)) {
			if (df.getSubfield('a') != null && !df.getSubfield('a').getData().isEmpty()
					&& df.getSubfield('2') != null && df.getSubfield('2').getData().equals(subfield2)) {
				results.add(SolrUtils.getVizFieldCode(source, fieldTag, df.getSubfield('a').getData().toLowerCase()));
			}
		}
		return results;
	}

	public List<String> getAuthAuthors(String tag) {
		List<String> result = new ArrayList<>();
		for (DataField df : record.getDataFields(tag)) {
			result.add(SolrUtils.getNameForDisplay(df));
		}
		return result;
	}

	public String getFirstAuthAuthor(String tag) {
		List<String> result = getAuthAuthors(tag);
		if (result.isEmpty()) return null;
		return result.get(0);
	}

	public List<String> getAuthIds(String tags) {
		List<String> result = new ArrayList<>();
		for (String tag : SPLIT_COLON.split(tags)) {
			for (DataField df : record.getDataFields(tag)) {
				if (df.getSubfield('7') == null) result.add("");
				else result.add(df.getSubfield('7').getData());
			}
		}
		return result;
	}

	public Set<String> getConspectus() throws IOException {
		Set<String> result = new HashSet<>();
		for (DataField df : record.getDataFields("072")) {
			if ((df.getSubfield('2') != null) && (df.getSubfield('2').getData().equals("Konspekt"))
					&& (df.getSubfield('9') != null) && (df.getSubfield('x') != null && (df.getSubfield('a') != null))) {
				String subcat_code_source = df.getSubfield('a').getData().trim();
				String subcat_name_source = df.getSubfield('x').getData().trim();
				String cat_code_source = df.getSubfield('9').getData().trim();

				boolean cat_code_exists = false;
				List<String> cat_code = translate(MAP_CONSPECTUS_SUBCAT_CAT_CHANGE, subcat_code_source, null);
				if (cat_code != null) {
					String[] split = SPLIT_VERTICAL_BAR.split(cat_code.get(0));
					if (split[1].equals(cat_code_source)) {
						cat_code_source = split[0];
						cat_code_exists = true;
					}
				}
				if (!cat_code_exists) {
					cat_code = translate(MAP_CATEGORY_SUBCATEGORY, subcat_code_source, null);
					if (cat_code == null || !cat_code.contains(cat_code_source)) continue;
				}
				List<String> subcat_name = translate(MAP_SUBCATEGORY_NAME, subcat_code_source, null);

				if (subcat_name != null && subcat_name.contains(subcat_name_source)) {
					String subcat_name_for_facet;
					List<String> subcat_name_temp = translate(MAP_CONSPECTUS_NAMES, subcat_code_source + " - " + subcat_name_source, null);
					if (subcat_name_temp != null && !subcat_name_temp.isEmpty()) {
						subcat_name_for_facet = subcat_name_temp.get(0);
					} else subcat_name_for_facet = subcat_name_source;
					List<String> category = translate(MAP_CONSPECTUS_CATEGORY, cat_code_source, null);
					result.addAll(SolrUtils.createHierarchicFacetValues(category.get(0), subcat_name_for_facet));
				}
			}
		}
		return result;
	}

	public Set<String> getAuthorAutocomplete(String tags) {
		Set<String> result = new HashSet<>();
		for (String author : getFields(tags)) {
			author = CleaningUtils.replaceAll(author, COMMA_PATTERN, "");
			result.add(SolrUtils.removeEndParentheses(author));
		}
		return result;
	}

	public String get773link() {
		for (DataField df : record.getDataFields("773")) {
			for (char code : new char[]{'x', 'z', 't'}) {
				Subfield sf = df.getSubfield(code);
				if (sf != null) {
					switch (sf.getCode()) {
					case 'x':
						if (ISSNUtils.isValid(sf.getData())) {
							return LINK773_ISSN + sf.getData();
						}
						if (sf.getData().startsWith("M")) return LINK773_ISMN + sf.getData();
						break;
					case 'z':
						String isbn = ISBNUtils.toISBN13String(sf.getData());
						if (isbn != null) return String.format(LINK773_ISBN, isbn);
						else continue;
					case 't':
						return LINK773_TITLE + sf.getData();
					default:
						break;
					}
				}
			}
		}
		return null;
	}

	public String get773display() {
		List<String> result = new ArrayList<>();
		for (DataField df : record.getDataFields("773")) {
			for (char code : new char[]{'t', 'x', 'g'}) {
				Subfield sf = df.getSubfield(code);
				if (code == 'x') {
					if (sf != null) {
						if (sf.getData().startsWith("M")) result.add(DISPLAY773_ISMN + sf.getData());
						else result.add(DISPLAY773_ISSN + sf.getData());
					} else {
						sf = df.getSubfield('z');
						if (sf != null) result.add(DISPLAY773_ISBN + sf.getData());
					}
				} else { // 't', 'g'
					if (sf == null) continue;
					result.add(sf.getData());
				}
			}
			if (!result.isEmpty()) {
				return String.join(DISPLAY773_JOINER, result);
			}
		}
		return null;
	}

	public String getAuthorityId() {
		return metadataRecord.getAuthorityId();
	}

	/**
	 * remove dot at the end of 700d
	 */
	public List<String> getAuthorFacet() {
		Set<String> results = new HashSet<>();
		results.addAll(getFields("100abcdq:975abcdq"));
		char[] sfCodes = {'a', 'b', 'c', 'd', 'q'};
		for (DataField df : record.getDataFields("700")) {
			StringBuilder author = new StringBuilder();
			for (char c : sfCodes) {
				if (df.getSubfield(c) != null) {
					author.append(df.getSubfield(c).getData()).append(' ');
					if (c == 'd') {
						author = new StringBuilder(author.toString().trim());
						if (author.toString().endsWith("."))
							author = new StringBuilder(author.substring(0, author.length() - 1));
					}
				}
			}
			if (author.length() > 0) results.add(author.toString().trim());
		}
		return new ArrayList<>(results);
	}

	public List<String> getInternationalPatentClassfication() {
		return metadataRecord.getInternationalPatentClassfication();
	}

	public Boolean getMetaproxyBool() {
		if (this.context.harvestedRecord().getHarvestedFrom().isMetaproxyEnabled()) {
			return metadataRecord.getMetaproxyBool();
		}
		return false;
	}

	public boolean getIndexWhenMerged() {
		return metadataRecord.getIndexWhenMerged();
	}

	public List<String> getBarcodes() {
		return metadataRecord.getBarcodes();
	}

	public List<String> getFormat() {
		return SolrUtils.createRecordTypeHierarchicFacet(metadataRecord.getDetectedFormatList());
	}

	public List<String> getInstitutionFacet() {
		return SolrUtils.getInstitution(context.harvestedRecord().getHarvestedFrom());
	}

	public List<String> getRegionInstitutionFacet() {
		return SolrUtils.getRegionInstitution(context.harvestedRecord().getHarvestedFrom());
	}

	public List<String> getInstitutionViewFacet() {
		return Collections.singletonList(SolrUtils.getInstitutionOfRecord(context.harvestedRecord().getHarvestedFrom()));
	}

	public String getSfxUrl() {
		return metadataRecord.getSfxUrl(context.harvestedRecord().getUniqueId().getRecordId());
	}

	public List<String> getIsmns() {
		List<String> results = new ArrayList<>();
		List<Ean> eans;
		if (results.isEmpty() && (!(eans = metadataRecord.getEANs()).isEmpty())
				&& metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.MUSICAL_SCORES)) {
			for (Ean ean : eans) {
				Long year = metadataRecord.getPublicationYear();
				results.add((year != null && year >= 2008L) ? ean.getEan().toString()
						: 'M' + ean.getEan().toString().substring(4));
			}
		}
		results.addAll(record.getFields("024", field -> field.getIndicator1() == '2', 'a'));
		return results;
	}

	public Set<String> getFormatForSearching() {
		Set<String> results = new HashSet<>();
		for (HarvestedRecordFormatEnum harvestedRecordFormatEnum : metadataRecord.getDetectedFormatList()) {
			try {
				List<String> data = translate(MAP_FORMAT_SEARCH, harvestedRecordFormatEnum.name().toLowerCase(), null);
				if (data != null) results.addAll(Arrays.asList(SPLIT_SEMICOLON.split(data.get(0))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	/**
	 * @return {@link Set} of string values of {@link ViewType}
	 */
	public Set<String> getViewType() {
		Long importConfId = context.harvestedRecord().getHarvestedFrom().getId();
		return new HashSet<>(ViewType.getPossibleValues(metadataRecord, listResolver, importConfId));
	}

	private static final String AUTH_PSEUDONYMS_NAME = "%s %s";

	public List<String> getAuthorityPseudonymsNames() {
		List<String> results = new ArrayList<>();
		for (DataField df : record.getDataFields("500")) {
			results.add(String.format(AUTH_PSEUDONYMS_NAME,
					SolrUtils.getSubfieldAsString(df, 'a'), SolrUtils.getSubfieldAsString(df, 'd')).trim());
		}
		return results;
	}

	public List<String> getAuthorityPseudonymsIds() {
		List<String> results = new ArrayList<>();
		for (DataField df : record.getDataFields("500")) {
			results.add(SolrUtils.getSubfieldAsString(df, '7'));
		}
		return results;
	}

	public Set<String> toLowerCase(Collection<String> collection) {
		return collection.stream().map(String::toLowerCase).collect(Collectors.toSet());
	}

	public String toLowerCase(String text) {
		return text.toLowerCase();
	}

	public Set<String> getOclcs() {
		return context.metadataRecord().getOclcs().stream().map(Oclc::getOclcStr).collect(Collectors.toSet());
	}

	public Set<String> getGenreFacet(String tags) {
		if (!metadataRecord.genreFacet()) return Collections.emptySet();
		return new HashSet<>(getFields(tags));
	}

	public List<String> getSeriesForSearching() {
		return record.getFields("787", field -> field.getIndicator1() == '0' && field.getIndicator2() == '8'
				&& field.getSubfield('i') != null
				&& Z_CYKLU_787.matcher(field.getSubfield('i').getData()).find(), 't');
	}

	public List<String> getSeriesForDisplay() {
		return record.getFields("787", field -> field.getIndicator1() == '0' && field.getIndicator2() == '8'
						&& field.getSubfield('i') != null
						&& Z_CYKLU_787.matcher(field.getSubfield('i').getData()).find(),
				SubfieldExtractionMethod.JOINED, "|", 't', 'g');
	}

}
