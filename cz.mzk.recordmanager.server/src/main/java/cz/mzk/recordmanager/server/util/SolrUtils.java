package cz.mzk.recordmanager.server.util;

import com.google.common.base.Preconditions;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolrUtils {

	private static final String WILDCARD = "*";

	private static final String RANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static final char HIERARCHIC_FACET_SEPARATOR = '/';

	private static final Pattern RECORDTYPE_PATTERN = Pattern.compile("^(AUDIO|VIDEO|OTHER|LEGISLATIVE|PATENTS|BLIND)_(.*)$");
	private static final Pattern PUBLISHER_NAME_BRACKET_PATTERN = Pattern.compile("[<>\\[\\]]");
	private static final Pattern PUBLISHER_NAME_CLEAN_END_PATTERN = Pattern.compile("[,?\\s]+$");
	private static final Pattern REMOVE_END_PUNCTUATION_PATTERN = Pattern.compile("[,;:/\\s]+$");
	private static final Pattern MATCH_END_PUNCTUATION_PATTERN = Pattern.compile("[^.]\\.\\.$");
	private static final Pattern AUTHOR_PATTERN = Pattern.compile("([^,]+),(.+)");

	private static final String INSTITUTION_LIBRARY = "Library";
	private static final String INSTITUTION_OTHERS = "Others";
	private static final String INSTITUTION_UNKNOWN = "unknown";

	private enum WEIGHT_DOC_COMPARATOR implements Comparator<SolrInputDocument> {
		INSTANCE;

		@Override
		public int compare(SolrInputDocument first, SolrInputDocument second) {
			int firstWeight = getWeight(first);
			int secondWeight = getWeight(second);
			return secondWeight - firstWeight;
		}

		private int getWeight(SolrInputDocument document) {
			int weight = 0;
			Object value = document.getFieldValue(SolrFieldConstants.WEIGHT);
			if (value != null && value instanceof Integer) {
				weight = (Integer) value;
			}
			return weight;
		}

	}

	public static String escape(String val) {
		return ClientUtils.escapeQueryChars(val);
	}

	public static String createFieldQuery(String field, String value) {
		return String.format("%s:%s", field, value);
	}

	public static String createEscapedFieldQuery(String field, String value) {
		return String.format("%s:%s", field, escape(value));
	}

	public static String createRange(String from, String to) {
		from = (from != null)? ClientUtils.escapeQueryChars(from) : WILDCARD;
		to = (to != null)? ClientUtils.escapeQueryChars(to) : WILDCARD;
		return String.format("[%s TO %s]", from,
				to);
	}

	public static String createDateRange(Date from, Date until) {
		// 'Z' on the end is necessary.. could not find a way how to put one
		// letter timezone and other letters than Z do not seem to work with
		// SOLR via Kramerius API anyway
		DateFormat df = new SimpleDateFormat(RANGE_DATE_FORMAT);
		String fromStr = (from != null) ? df.format(from) : null;
		String untilStr = (until != null) ? df.format(until) : null;
		return createRange(fromStr, untilStr);
	}

	public static void sortByWeight(List<SolrInputDocument> childs) {
		Collections.sort(childs, WEIGHT_DOC_COMPARATOR.INSTANCE);
	}

	public static List<SolrInputDocument> removeHiddenFields(List<SolrInputDocument> documents) {
		for (SolrInputDocument document : documents) {
			removeHiddenFields(document);
		}
		return documents;
	}

	public static SolrInputDocument removeHiddenFields(SolrInputDocument document) {
		Preconditions.checkNotNull(document, "document");
		Iterator<String> iter = document.getFieldNames().iterator();
		while (iter.hasNext()) {
			String field = iter.next();
			if (field.startsWith(SolrFieldConstants.SOLR_HIDDEN_FIELD_PREFIX)) {
				iter.remove();
			}
		}
		if (document.hasChildDocuments()) {
			removeHiddenFields(document.getChildDocuments());
		}
		return document;
	}

	/**
	 *
	 * Create hierarchic facet values, ie. for input Brno, MZK returns:
	 *
	 * 0/Brno/
	 * 1/Brno/MZK/
	 *
	 * @param values input values
	 * @return hierarchic facet values
	 */
	public static List<String> createHierarchicFacetValues(String...values) {
		List<String> result = new ArrayList<>(values.length);
		for (int i = 0; i != values.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(i);
			sb.append(HIERARCHIC_FACET_SEPARATOR);
			for (int j = 0; j!= i + 1; j++) {
				sb.append(values[j]);
				sb.append(HIERARCHIC_FACET_SEPARATOR);
			}
			result.add(sb.toString());
		}
		return result;
	}

	public static List<String> createHierarchicFacetValues(final Collection<String> statuses) {
		List<String> results = new ArrayList<>();
		for (String status : statuses) {
			if (status.equals(Constants.DOCUMENT_AVAILABILITY_UNKNOWN)
					|| status.equals(Constants.DOCUMENT_AVAILABILITY_ONLINE)
					|| status.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) {
				results.addAll(createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, status));
			} else results.addAll(createHierarchicFacetValues(status));
		}
		return results;
	}

	public static List<String> createRecordTypeHierarchicFacet(HarvestedRecordFormatEnum format) {
		Matcher matcher = RECORDTYPE_PATTERN.matcher(format.name());
		if (matcher.matches()) {
			return SolrUtils.createHierarchicFacetValues(matcher.group(1), matcher.group(2));
		}
		else {
			return SolrUtils.createHierarchicFacetValues(format.name());
		}
	}

	public static List<String> createRecordTypeHierarchicFacet(Collection<HarvestedRecordFormatEnum> formats) {
		List<String> results = new ArrayList<>();
		for (HarvestedRecordFormatEnum format : formats) {
			results.addAll(createRecordTypeHierarchicFacet(format));
		}
		return results;
	}

	public static String getInstitutionOfRecord(ImportConfiguration config) {
		if (config != null && config.getLibrary() != null
				&& config.getLibrary().getName() != null) {
			return config.getLibrary().getName();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

	protected static String getCityOfRecord(ImportConfiguration config) {
		if (config != null && config.getLibrary() != null
				&& config.getLibrary().getCity() != null) {
			return config.getLibrary().getCity();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

	protected static String getPrefixOfNKPRecord(ImportConfiguration config) {
		if (config != null) {
			if (config.getId() != null) {
				if (config.getId() == Constants.IMPORT_CONF_ID_SLK)
					return Constants.LIBRARY_NAME_SLK;
				if (config.getId() == Constants.IMPORT_CONF_ID_KKL)
					return Constants.LIBRARY_NAME_KKL;
				if (config.getId() == Constants.IMPORT_CONF_ID_STT)
					return Constants.LIBRARY_NAME_STT;
			}
			if (config.getIdPrefix() != null) {
				return config.getIdPrefix().toUpperCase();
			}
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

	protected static String getRegionOfRecord(ImportConfiguration config) {
		if (config != null && config.getLibrary() != null
				&& config.getLibrary().getRegion() != null) {
			return config.getLibrary().getRegion();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

	public static List<String> getRegionInstitution(ImportConfiguration config) {
		if (config != null) {
			String region = getRegionOfRecord(config);
			region = region == null ? INSTITUTION_UNKNOWN : region;
			String name = getInstitutionOfRecord(config);
			if (name.equals(Constants.LIBRARY_NAME_NKP)) {
				String prefix = getPrefixOfNKPRecord(config);
				return SolrUtils.createHierarchicFacetValues(INSTITUTION_LIBRARY, region, name, prefix);
			}
			String base = config.isLibrary() ? INSTITUTION_LIBRARY : INSTITUTION_OTHERS;
			if (region.equals(INSTITUTION_UNKNOWN)) return SolrUtils.createHierarchicFacetValues(base, name);
			else return SolrUtils.createHierarchicFacetValues(base, region, name);
		}

		return SolrUtils.createHierarchicFacetValues(INSTITUTION_UNKNOWN);
	}

	public static String getAllFieldsString(final List<DataField> dataFields) {
		StringBuilder builder = new StringBuilder();
		for (DataField df : dataFields) {
			for (Subfield subfield : df.getSubfields()) {
				if (builder.length() > 0) builder.append(' ');
				builder.append(subfield.getData());
			}
		}
		return builder.toString();
	}

	/**
	 * remove end parenthesis from the end of value if there is no start parenthesis
	 * for author and subject autocomplete
	 * @param collection of values
	 * @return {@link Set} of filtered values
	 */
	private static final Pattern END_PARENTHESIS_PATTERN = Pattern.compile("\\)$");
	private static final String START_PARENTHESIS_STR = "(";
	private static final String END_PARENTHESIS_STR = ")";

	public static Set<String> removeEndParentheses(Collection<String> collection) {
		Set<String> results = new HashSet<>();
		for (String value : collection) {
			results.add(removeEndParentheses(value));
		}
		return results;
	}

	public static String removeEndParentheses(String value) {
		if (value != null && value.endsWith(END_PARENTHESIS_STR) &&
				(StringUtils.countMatches(value, START_PARENTHESIS_STR) < StringUtils.countMatches(value, END_PARENTHESIS_STR))) {
			return CleaningUtils.replaceFirst(value, END_PARENTHESIS_PATTERN, "");
		}
		return value;
	}

	public static int getInd2AsInt(DataField df) {
		char ind2char = df.getIndicator2();
		return Character.isDigit(ind2char) ? Integer.valueOf(String.valueOf(ind2char)) : 0;
	}

	public static String cleanPublisherName(String name) {
		name = CleaningUtils.replaceAll(name, PUBLISHER_NAME_BRACKET_PATTERN, "");
		name = CleaningUtils.replaceAll(name, PUBLISHER_NAME_CLEAN_END_PATTERN, "");
		return name.trim();
	}

	public static String toUpperCaseFirstChar(String string) {
		if (string == null || string.isEmpty()) return null;
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	public static List<String> toUpperCaseFirstChar(List<String> strings) {
		if (strings == null || strings.isEmpty()) return Collections.emptyList();
		List<String> results = new ArrayList<>();
		strings.forEach(string -> results.add(string.substring(0, 1).toUpperCase() + string.substring(1)));
		return results;
	}

	public static String removeEndPunctuation(String data) {
		if (data == null || data.isEmpty()) return null;
		data = CleaningUtils.replaceAll(data, REMOVE_END_PUNCTUATION_PATTERN, "");
		if (MATCH_END_PUNCTUATION_PATTERN.matcher(data).find()) data = data.substring(0, data.length() - 1);
		return data;
	}

	public static String getNameForDisplay(DataField df) {
		StringBuilder sb = new StringBuilder();
		sb.append(changeNameMarc(df));

		for (char subfield : new char[]{'b', 'c', 'd'}) {
			if (df.getSubfield(subfield) != null) {
				sb.append(' ');
				sb.append(df.getSubfield(subfield).getData());
			}
		}
		return removeEndPunctuation(sb.toString().trim());
	}

	private static String changeNameMarc(DataField df) {
		StringBuilder sb = new StringBuilder();
		if (df.getIndicator1() == '1') {
			String suba = "";
			if (df.getSubfield('a') != null) suba = df.getSubfield('a').getData();
			Matcher matcher = AUTHOR_PATTERN.matcher(suba);
			if (matcher.matches()) {
				sb.append(removeEndPunctuation(matcher.group(2)));
				sb.append(' ');
				sb.append(matcher.group(1));
				sb.append(',');
			} else sb.append(suba);
		} else {
			if (df.getSubfield('a') != null) sb.append(df.getSubfield('a').getData());
		}

		return sb.toString();
	}

	public static String changeNameDC(String name) {
		if (name == null || name.isEmpty()) return null;

		StringBuilder sb = new StringBuilder();
		Matcher matcher = AUTHOR_PATTERN.matcher(name);
		if (matcher.matches()) {
			sb.append(matcher.group(2));
			sb.append(' ');
			sb.append(matcher.group(1));
		} else return name;

		return sb.toString();
	}

	public static String getNameForExact(DataField df) {
		StringBuilder sb = new StringBuilder();
		sb.append(SolrUtils.changeNameMarc(df));

		if (df.getSubfield('b') != null) {
			sb.append(' ');
			sb.append(df.getSubfield('b').getData());
		}

		return removeEndPunctuation(sb.toString().trim());
	}

	public static String getVizFieldCode(String source, String fieldTag, String value) {
		return source + '|' + fieldTag + '|' + value;
	}

	public static String getSubfieldAsString(DataField df, char subfieldCode) {
		return df.getSubfield(subfieldCode) != null ? df.getSubfield(subfieldCode).getData() : "";
	}

}
