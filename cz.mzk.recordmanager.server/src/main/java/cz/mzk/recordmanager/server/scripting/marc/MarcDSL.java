package cz.mzk.recordmanager.server.scripting.marc;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cz.mzk.recordmanager.server.model.Ean;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import com.google.common.primitives.Chars;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.scripting.BaseDSL;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.identifier.ISSNUtils;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class MarcDSL extends BaseDSL {

	private MetadataRecord metadataRecord;

	private final static String EMPTY_SEPARATOR = "";
	private final static String SPACE_SEPARATOR = " ";
	private final static String END_PUNCTUATION = "[:,=;/.]+$";
	private final static String LEAD_SPACE = "^ *";
	private final static String PACK_SPACES = " +";
	private final static String NUMBERS = "([0-9])[.,]([0-9])";
	private final static String SUPPRESS = "<<[^<{2}]*>>";
	private final static String TO_BLANK = "['\\[\\]\"`!()\\-{};:.,?/@*%=^_|~]";

	private final static String MAP_CATEGORY_SUBCATEGORY = "conspectus_category_subcategory.map";
	private final static String MAP_SUBCATEGORY_NAME = "conspectus_subcategory_name.map";
	private final static String MAP_CONSPECTUS_NAMES = "conspectus_names.map";
	private final static String MAP_CONSPECTUS_CATEGORY = "conspectus_category.map";
	private final static String MAP_CONSPECTUS_SUBCAT_CAT_CHANGE = "conspectus_category_change.map";
	private final static String MAP_FORMAT_SEARCH = "format_search.map";
	
	private final static Pattern FIELD_PATTERN = Pattern
			.compile("([0-9]{3})([a-zA-Z0-9]*)");
	private final static Pattern AUTHOR_PATTERN = Pattern
			.compile("([^,]+),(.+)");

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
			Map<String, RecordFunction<MarcFunctionContext>> functions) {
		super(propertyResolver, stopWordsResolver);
		this.context = context;
		this.record = context.record();
		this.functions = functions;
		this.metadataRecord = context.metadataRecord();
	}

	public MarcRecord getRecord() {
		return record;
	}

	public String getFirstField(String tags) {
		for (String tag : tags.split(":")) {
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

	public List<String> getFields(String tags) {
		return this.getFields(tags, SubfieldExtractionMethod.JOINED);
	}

	public List<String> getFields(String tags, SubfieldExtractionMethod method) {
		List<String> result = new ArrayList<String>(0);
		for (String tag : tags.split(":")) {
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
			language = language.replaceAll("\\W", "");
			results.add(language);
		}
		return results;
	}
	
	public String getCountry(){
		String f008 = record.getControlField("008");
		if (f008 != null && f008.length() > 18) {
			return f008.substring(15, 18).trim();
		}
		String s = getFirstField("044a");
		if(s != null) return getFirstField("044a").trim();
		
		return "";
	}

	public Set<String> getCountries() {
		Set<String> countries = new HashSet<>();
		String f008 = record.getControlField("008");
		if (f008 != null && f008.length() > 18) {
			countries.add(f008.substring(15, 18).trim());
		}
		countries.addAll(getFields("044a", SubfieldExtractionMethod.SEPARATED));
		countries.forEach(c -> c.trim());
		return countries;
	}

    /*
     * Get all fields starting with the 100 and ending with the 839
     * This will ignore any "code" fields and only use textual fields
     */
	public List<String> getAllFields() {
		Map<String, List<DataField>> allFields = record.getAllFields();
		List<String> result = new ArrayList<String>();
        for (Entry<String, List<DataField>> entry : allFields.entrySet()) {
        	int tag = -1;
        	try {
        		tag = Integer.parseInt(entry.getKey());
        	} catch (NumberFormatException nfe) {
        		continue;
        	}
            if ((tag < 100) || (tag >= 840)) {
            	continue;
            }
            List<DataField> fields = entry.getValue();
            StringBuffer buffer = new StringBuffer();
			for (DataField field : fields) {
                List<Subfield> subfields = field.getSubfields();
                Iterator<Subfield> subfieldsIter = subfields.iterator();
                while (subfieldsIter.hasNext()) {
                    Subfield subfield = (Subfield) subfieldsIter.next();
                    if (buffer.length() > 0) {
                        buffer.append(" " + subfield.getData());
                    } else {
                        buffer.append(subfield.getData());
                    }
                }
            }
			result.add(buffer.toString());
        }
        return result;
	}

    /**
     * Get the title (245ab) from a record, without non-filing chars as
     * specified in 245 2nd indicator, and lowercased. 
     * @param context - the marc record object
     * @return 245a and 245b values concatenated, with trailing punct removed,
     *         and with non-filing characters omitted. Null returned if no
     *         title can be found. 
     * 
     * @see SolrIndexer#getTitle
     */
    public String getSortableTitle() {
    	List<DataField> titleFields = record.getAllFields().get("245");
    	if (titleFields == null || titleFields.isEmpty()) {
    		return "";
    	}
        DataField titleField = titleFields.get(0);
        if (titleField == null)
            return "";

        int nonFilingInt = getInd2AsInt(titleField);
        
        List<Title> titles = metadataRecord.getTitle();
        if (titles == null || titles.isEmpty()) return null;
        String title = metadataRecord.getTitle().get(0).getTitleStr();
        title = title.replaceAll(END_PUNCTUATION, EMPTY_SEPARATOR);
        title = title.replaceAll(NUMBERS, "$1$2");
        title = title.toLowerCase();
        
        //Skip non-filing chars, if possible. 
        if (title.length() > nonFilingInt )  {
          title = title.substring(nonFilingInt);          
        }
        
        if ( title.length() == 0) {
          return null;
        }                
        
        title = title.replaceAll(SUPPRESS, EMPTY_SEPARATOR);
        title = title.replaceAll(TO_BLANK, SPACE_SEPARATOR);
        title = title.replaceAll(LEAD_SPACE, EMPTY_SEPARATOR);
        title = title.replaceAll(PACK_SPACES, SPACE_SEPARATOR);
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
	
    protected int getInd2AsInt(DataField df) {
        char ind2char = df.getIndicator2();
        int result = 0;
        if (Character.isDigit(ind2char))
            result = Integer.valueOf(String.valueOf(ind2char));
        return result;
    }
    
    public List<String> getPublisherStrMv() throws IOException{
    	Set<String> publishers = new HashSet<String>();
    	for(DataField dataField: record.getDataFields("264")){
    		if(dataField.getIndicator2() == '1'){
    			dataField.getSubfields('b').stream().forEach(sf -> publishers.add(editPublisherName(sf.getData())));
    		}
    	}
    	for(DataField dataField: record.getDataFields("928")){
    		if(dataField.getIndicator1() == '9'){
    			dataField.getSubfields('a').stream().forEach(sf -> publishers.add(editPublisherName(sf.getData())));
    		}
    	}
    	getFields("260b:978ab").stream().forEach(str -> publishers.add(editPublisherName(str)));
    	
    	Set<String> result = new HashSet<String>();
    	for(String publisher: publishers){
    		List<String> newPublisher = translate("publisher.map", publisher, null);
    		if(newPublisher == null) result.add(publisher);
    		else result.addAll(newPublisher);
    	}
    	return new ArrayList<String>(result);
    }
    
    public String editPublisherName(String name){
    	name = name.replaceAll("[<>\\[\\]]", "");
    	name = name.replaceAll("[,?\\s]+$", "");
    	name = name.trim();
    	return name;
    }
    
    public List<String> getPublisher(){
    	List<String> publishers = new ArrayList<String>();
    	for(DataField dataField: record.getDataFields("264")){
    		if(dataField.getIndicator2() == '1'){
    			publishers.addAll(getFieldsTrim("264b"));
    		}
    	}
    	publishers.addAll(getFieldsTrim("260b"));
    	
    	return publishers;
    }

    public Set<String> getFieldsTrim(String tags){
    	Set<String> result = new HashSet<String>();
    	for(String data: getFields(tags)){
    		result.add(removeEndPunctuation(data));
    	}
    	return result;
    }

	public Set<String> getFieldsUnique(String tags) {
		return this.getFieldsUnique(tags, SubfieldExtractionMethod.JOINED);
	}

	public Set<String> getFieldsUnique(String tags, SubfieldExtractionMethod method) {
		Set<String> result = new HashSet<String>();
		result.addAll(getFields(tags, method));
		return result;
	}

    public String getFirstFieldTrim(String tags){
    	return removeEndPunctuation(getFirstField(tags));
    }
    
    public Set<String> getSubject(String tags) throws IOException{
    	Set<String> subjects = new HashSet<String>();

    	for(String subject: getFields(tags)){
    		subjects.add(toUpperCaseFirstChar(subject));
    	}

    	for(DataField df: record.getDataFields("653")){
    		for(Subfield sf: df.getSubfields('a')){ 
    			if(!sf.getData().matches("forma:.*|nosič:.*|způsob vydávání:.*|úroveň zpracování:.*"))
    				subjects.add(toUpperCaseFirstChar(sf.getData()));
    		}
    	}
    	
    	for(DataField df: record.getDataFields("650")){
    		if(df.getSubfield('2') != null && df.getSubfield('2').getData().contains("psh")){
    			if(df.getSubfield('x') != null){ 
    				subjects.addAll(toUpperCaseFirstChar(translate("psh.map", df.getSubfield('x').getData(), null)));
    			}
    		}
    	}
    	
    	if (metadataRecord.filterSubjectFacet() != null) {
    		return new HashSet<String>(filter(metadataRecord.filterSubjectFacet(), new ArrayList<String>(subjects)));
    	}
    	
    	return subjects;
    }
    
    protected String toUpperCaseFirstChar(String string){
    	if(string == null || string.isEmpty()) return null;
    	return string.substring(0,1).toUpperCase() + string.substring(1);
    }
    
    protected List<String> toUpperCaseFirstChar(List<String> strings){
    	if(strings == null || strings.isEmpty()) return Collections.emptyList();
    	List<String> results = new ArrayList<>();
    	strings.forEach(string -> results.add(string.substring(0,1).toUpperCase() + string.substring(1)));
    	return results;
    }

    public Set<String> getISBNISSNISMN(){
    	Set<String> result = new HashSet<String>();
    	
    	for(DataField df: record.getDataFields("024")){
    		if(df.getIndicator1() == '2'){
    			result.addAll(getFields("024az"));
    		}
    	}
    	result.addAll(getFields("020az:022az:787xz:902a"));    	
    	
    	return result;
    }
    
    public String getId001(){
    	return record.getControlField("001");
    }
    
    public Set<String> getTitleSeries(){
    	Set<String> result = new HashSet<String>();
    	result.addAll(getFieldsTrim("130adfgklnpst7:210a:222ab:240adklmprs:242ap:245abnp:246anp:247afp:"
    			+ "440a:490anp:700klmnoprst7:710klmnoprst7:711klmnoprst7:730adklmprs7:740anp:765ts9:"
    			+ "773kt:780st:785st:787st:800klmnoprst7:810klmnoprst7:811klmnoprst7:830aklmnoprst7"));
    	for(DataField df: record.getDataFields("505")){
    		if(df.getIndicator2() == '0'){
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
    	for (DataField dataField: list996) {
    		StringBuilder currentSb = new StringBuilder();
    		// 996 with '0' in subfield 'q'
    		if (dataField.getSubfield('q') != null && dataField.getSubfield('q').getData().equals("0")) {
    			continue;
    		}
    		for (Subfield subfield: dataField.getSubfields()) {
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
    	for (DataField df: record.getDataFields("866")) {
    		String subS = "", subX="";
    		
    		if (df.getSubfield('s') != null) {
    			subS = df.getSubfield('s').getData();
    		}
    		if (df.getSubfield('x') != null) {
    			subX = df.getSubfield('x').getData();
    		}
    		
    		if (!subS.isEmpty()) {
    			result.add(subS + "|" + subX);
    		}
    	}
    	return result;
    }

    public Long getLoanRelevance(){
		Long count = 0L;
    	
    	for(DataField df: record.getDataFields("996")){
    		if(df.getSubfield('n') != null)
    		try {
    			count += Long.valueOf(df.getSubfield('n').getData());
    		} catch (NumberFormatException nfe) {
    		}			
		}
    	return count; 
    }

    public String getAuthorForSorting(){
		List<String> authors = getFields("100abcd:110abcd:111abcd:700abcd:710abcd:711abcd");
		if(authors == null || authors.isEmpty()) return null;
		String author = authors.get(0);
		author = author.toLowerCase();
		author = author.replaceAll(END_PUNCTUATION, EMPTY_SEPARATOR);
		author = author.replaceAll(SUPPRESS, EMPTY_SEPARATOR);
		author = author.replaceAll(TO_BLANK, SPACE_SEPARATOR);
		author = author.replaceAll(LEAD_SPACE, EMPTY_SEPARATOR);
		author = author.replaceAll(PACK_SPACES, SPACE_SEPARATOR);
		if(author.isEmpty()) return null;
		return author;
    }
    
    public String getCitationRecordType(){
    	return metadataRecord.getCitationFormat().getCitationType();
    }
    
    public String getTitleDisplay(){
		DataField df = getFirstDataField("245");
		if(df == null) return null;
		
		final char titleSubfields[] = new char[]{'a','b','n','p'};
		final char sfhPunctuation[] = new char[]{'.',',',':'};
		char endCharH = ' ';
		StringBuilder sb = new StringBuilder();

		for(Subfield sf: df.getSubfields()){
			// get last punctuation from 'h'
			if(sf.getCode() == 'h'){
				String data = sf.getData().trim();
				if(data.length() > 0){
					if(Chars.contains(sfhPunctuation, data.charAt(data.length() - 1))){
						endCharH = data.charAt(data.length() - 1);
					}
				}
			}
			else if(Chars.contains(titleSubfields, sf.getCode())){
				// print punctuation from h
				if(endCharH != ' '){
					sb.append(endCharH);
					sb.append(" ");
					endCharH = ' ';
				}
				sb.append(sf.getData());
				sb.append(" ");
			}
			else endCharH = ' ';
		}
    	return removeEndPunctuation(sb.toString());
    }
    
    protected String removeEndPunctuation(String data){
    	if(data == null || data.isEmpty()) return null;
    	data = data.replaceAll("[,;:/\\s]+$", "");
    	if(data.matches(".*[^.]\\.\\.$")) data = data.substring(0, data.length()-1);
    	return data;
    }
    
    public DataField getFirstDataField(String tag){
    	List<DataField> list = record.getDataFields(tag);
    	if(list.isEmpty()) return null;
    	else return list.get(0);
    }

	public String getAuthorDisplay() {
		List<DataField> list = record.getDataFields("100");
		if (list.isEmpty()) return null;
		DataField df = list.get(0);
		String name = getNameForDisplay(df);
		if (name != null && name.isEmpty()) return null;
		else return name;
	}

	public List<String> getAuthor2Display(){
    	List<String> result = new ArrayList<String>();
    	for(DataField df: record.getDataFields("700")){
    		result.add(getNameForDisplay(df));
    	}
    	result.addAll(getFields("110ab:111ab:710ab:711ab"));
    	return result;
    }
    
    public String getNameForDisplay(DataField df) {
    	StringBuilder sb = new StringBuilder();
		sb.append(changeName(df));

		for(char subfield: new char[]{'b', 'c', 'd'}){
			if(df.getSubfield(subfield) != null) {
				sb.append(" ");
				sb.append(df.getSubfield(subfield).getData());
			}
		}
		return removeEndPunctuation(sb.toString().trim());
    }

	public String getAuthorExact() {
		List<DataField> list = record.getDataFields("100");
		if (list.isEmpty()) return null;
		DataField df = list.get(0);
		String name = getNameForExact(df);
		if (name != null && name.isEmpty()) return null;
		else return name;
	}

	public String getNameForExact(DataField df) {
    	StringBuilder sb = new StringBuilder();
		sb.append(changeName(df));

		if(df.getSubfield('b') != null) {
			sb.append(" ");
			sb.append(df.getSubfield('b').getData());
		}

		return removeEndPunctuation(sb.toString().trim());
    }
    
    public String changeName(DataField df){
    	StringBuilder sb = new StringBuilder();
		if(df.getIndicator1() == '1'){
			String suba = "";
			if(df.getSubfield('a') != null) suba = df.getSubfield('a').getData();
			Matcher matcher = AUTHOR_PATTERN.matcher(suba);
			if(matcher.matches()){
				sb.append(removeEndPunctuation(matcher.group(2)));
				sb.append(" ");
				sb.append(matcher.group(1));
				sb.append(",");
			}
			else sb.append(suba);
		}
		else{
			if(df.getSubfield('a') != null) sb.append(df.getSubfield('a').getData());
		}

		return sb.toString();
    }
    
    public List<String> getAuthorFind(){
    	List<String> result = new ArrayList<String>();
    	result.add(getAuthorDisplay());
    	result.addAll(getAuthor2Display());
    	return result;
    }
    
	public Set<String> getAuthorityIds(String tags) {
		Set<String> result = new HashSet<>();
		for (String tag : tags.split("\\|")) {
			String[] split = tag.split("\\.");
			for (String value : split[1].split(":")) {
				Matcher matcher = FIELD_PATTERN.matcher(value);
				if (!matcher.matches()) {
					throw new IllegalArgumentException("Tag can't be parsed: "
							+ value);
				}
				String fieldTag = matcher.group(1);
				String subFields = matcher.group(2);
				switch (split[0]) {
				case Constants.PREFIX_AUTH:
					record.getFields(fieldTag, " ", subFields.toCharArray()).stream()
							.forEach(s -> result.add(getVizFieldCode(split[0], fieldTag, s)));
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
			if (df.getSubfield('a') != null	&& df.getSubfield('2') != null
					&& df.getSubfield('2').getData().equals(subfield2)) {
				results.add(getVizFieldCode(source, fieldTag, df.getSubfield('a').getData().toLowerCase()));
			}
		}
		return results;
	}

	private String getVizFieldCode(String source, String fieldTag, String value) {
		return source + "|" + fieldTag + "|" + value;
	}

    public List<String> getAuthAuthors(String tag){
    	List<String> result = new ArrayList<>();
    	for(DataField df: record.getDataFields(tag)){
    		result.add(getNameForDisplay(df));
    	}
    	return result;
    }
    
    public String getFirstAuthAuthor(String tag){
    	List<String> result = getAuthAuthors(tag);
    	if(result.isEmpty()) return null;
    	return result.get(0);
    }
    
    public List<String> getAuthorityUrl(String tags){
    	List<String> urls = getFields(tags);
    	if(urls.isEmpty()) return Collections.emptyList();
    	if(urls.size() == 1) return urls;
    	for(String url: urls){
    		if(url.matches(".*wikipedia.*")) return Collections.singletonList(url);
    	}
    	return Collections.singletonList(urls.get(0));
    }
    
    public List<String> getAuthIds(String tags){
		List<String> result = new ArrayList<>();
		
		for(String tag : tags.split(":")) {
			for(DataField df: record.getDataFields(tag)){
				if(df.getSubfield('7') == null) result.add("");
				else result.add(df.getSubfield('7').getData());
			}
		}
    	return result;
    }
    
	public Set<String> getConspectus() throws IOException {
		Set<String> result = new HashSet<>();
		for (DataField df: record.getDataFields("072")) {
			if ((df.getSubfield('2') != null) && (df.getSubfield('2').getData().equals("Konspekt"))
					&& (df.getSubfield('9') != null) && (df.getSubfield('x') != null && (df.getSubfield('a') != null))) {
				String subcat_code_source = df.getSubfield('a').getData().trim();
				String subcat_name_source = df.getSubfield('x').getData().trim();
				String cat_code_source = df.getSubfield('9').getData().trim();

				boolean cat_code_exists = false;
				List<String> cat_code = translate(MAP_CONSPECTUS_SUBCAT_CAT_CHANGE, subcat_code_source, null);
				if (cat_code != null) {
					String[] split = cat_code.get(0).split("\\|");
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
					String subcat_name_for_facet = null;
					List<String> subcat_name_temp = translate(MAP_CONSPECTUS_NAMES, subcat_code_source + " - " + subcat_name_source, null);

					if (subcat_name_temp != null && !subcat_name_temp.isEmpty()) {
						subcat_name_for_facet = subcat_name_temp.get(0);
					}
					else subcat_name_for_facet = subcat_name_source;
					List<String> category = translate(MAP_CONSPECTUS_CATEGORY, cat_code_source, null);
					result.addAll(SolrUtils.createHierarchicFacetValues(category.get(0), subcat_name_for_facet));
				}
			}
		}

		return result;
	}
    
    public Set<String> getAuthorAutocomplete(String tags){
    	Set<String> result = new HashSet<>();
    	for(String s: getFields(tags)){
    		result.add(s.replaceAll(",", ""));
    	}
    	return result;
    }
    
    public String get773link(){
    	for(DataField df: record.getDataFields("773")){
    		for(char code: new char[]{'x', 'z', 't'}){
    			Subfield sf = df.getSubfield(code);
    			if(sf != null){
    				switch (sf.getCode()) {
					case 'x':
						if(ISSNUtils.isValid(sf.getData())){
							return LINK773_ISSN + sf.getData();
						}
						if(sf.getData().startsWith("M")) return LINK773_ISMN + sf.getData();
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
    
    public String get773display(){
    	List<String> result = new ArrayList<>();
    	for(DataField df: record.getDataFields("773")){
    		for(char code: new char[]{'t', 'x', 'g'}){
    			Subfield sf = df.getSubfield(code);
    			if(code == 'x'){
    				if(sf != null){
    					if(sf.getData().startsWith("M")) result.add(DISPLAY773_ISMN + sf.getData());
    					else result.add(DISPLAY773_ISSN + sf.getData());
    				}
    				else{
    					sf = df.getSubfield('z');
    					if(sf != null) result.add(DISPLAY773_ISBN + sf.getData());
    				}
    			}
    			else{ // 't', 'g'
    				if(sf == null) continue;
    				result.add(sf.getData());
    			}
    		}
    		if(!result.isEmpty()){
    			return String.join(DISPLAY773_JOINER, result);
    		}
    	}
    	return null;
    }
    
    public String getAuthorityId(){
    	return metadataRecord.getAuthorityId();
    }
    
    /**
     * remove dot at the end of 700d
     */
    public List<String> getAuthorFacet(String k){
		Set<String> results = new HashSet<>();
		results.addAll(getFields("100abcdq:975abcdq"));
		char[] sfCodes = new char[]{'a', 'b', 'c', 'd', 'q'};
		for (DataField df : record.getDataFields("700")) {
			String author = "";
			for (char c : sfCodes) {
				if (df.getSubfield(c) != null) {
					author += df.getSubfield(c).getData() + " ";
					if (c == 'd') {
						author = author.trim();
						if (author.endsWith(".")) author = author.substring(0, author.length()-1);
					}
				}
			}
			if (!author.isEmpty()) results.add(author.trim());
		}
    	return new ArrayList<>(results);
    }
    
	public List<String> getInternationalPatentClassfication() {
		return metadataRecord.getInternationalPatentClassfication();
	}

	public Boolean getMetaproxyBool() {
		return metadataRecord.getMetaproxyBool();
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

	public String get003() {
		return record.getControlField("003");
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
						: "M" + ean.getEan().toString().substring(4));
			}
		}
		results.addAll(record.getFields("024", field -> field.getIndicator1() == '2','a'));
		return results;
	}

	public Set<String> getFormatForSearching() {
		Set<String> results = new HashSet<>();
		for (HarvestedRecordFormatEnum harvestedRecordFormatEnum : metadataRecord.getDetectedFormatList()) {
			try {
				List<String> data = translate(MAP_FORMAT_SEARCH, harvestedRecordFormatEnum.name().toLowerCase(), null);
				if (data != null) results.addAll(Arrays.asList(data.get(0).split(";")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	public List<String> getViewType() {
		return metadataRecord.getViewType().stream().map(t -> t.getValue()).collect(Collectors.toList());
	}
}
