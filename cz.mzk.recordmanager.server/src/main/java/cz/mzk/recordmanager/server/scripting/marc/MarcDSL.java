package cz.mzk.recordmanager.server.scripting.marc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;

public class MarcDSL {
	
	private MetadataRecord marcMetadataRecord;
	
	private final static String EMPTY_SEPARATOR = "";
	
	private final static Pattern FIELD_PATTERN = Pattern
			.compile("([0-9]{3})([a-zA-Z0-9]*)");

	private final MarcRecord record;

	private final MappingResolver propertyResolver;
	
	private final Map<String, RecordFunction<MarcRecord>> functions;

	public MarcDSL(MarcRecord record, MappingResolver propertyResolver, Map<String, RecordFunction<MarcRecord>> functions) {
		super();
		this.record = record;
		this.propertyResolver = propertyResolver;
		this.functions = functions;
		this.marcMetadataRecord = new MetadataMarcRecord(record);
	}

	public String getFirstField(String tag) {
		Matcher matcher = FIELD_PATTERN.matcher(tag);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Tag can't be parsed: " + tag);
		}
		String fieldTag = matcher.group(1);
		String subFields = matcher.group(2);
		return record.getField(fieldTag, subFields.toCharArray());
	}

	public List<String> getFields(String tags) {
		List<String> result = new ArrayList<String>(0);
		for (String tag : tags.split(":")) {
			Matcher matcher = FIELD_PATTERN.matcher(tag);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Tag can't be parsed: "
						+ tag);
			}
			String fieldTag = matcher.group(1);
			String subFields = matcher.group(2);
			result.addAll(record.getFields(fieldTag, " ",
					subFields.toCharArray()));
		}
		return result;
	}

	public List<String> getLanguages() {
		List<String> languages = new ArrayList<String>();
		String f008 = record.getControlField("008");
		if (f008 != null && f008.length() > 38) {
			languages.add(f008.substring(35, 38));
		}
		languages.addAll(record.getFields("041", EMPTY_SEPARATOR, 'a'));
		languages.addAll(record.getFields("041", EMPTY_SEPARATOR, 'd'));
		languages.addAll(record.getFields("041", EMPTY_SEPARATOR, 'e'));
		return languages;
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

	public String translate(String file, String input, String defaultValue)
			throws IOException {
		Mapping mapping = propertyResolver.resolve(file);
		String result = (String) mapping.get(input);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}

	public List<String> translate(String file, List<String> inputs,
			String defaultValue) throws IOException {
		List<String> translated = new ArrayList<String>();
		Mapping mapping = propertyResolver.resolve(file);
		for (String input : inputs) {
			String result = (String) mapping.get(input);
			if (result == null) {
				result = defaultValue;
			}
			if (result != null) {
				translated.add(result);
			}
		}
		return translated;
	}

    /*
     * Get all fields starting with the 100 and ending with the 839
     * This will ignore any "code" fields and only use textual fields
     */
	public String getAllFields() {
		Map<String, List<DataField>> allFields = record.getAllFields();
		StringBuffer buffer = new StringBuffer();
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
        }
        return buffer.toString();
	}
	
    /**
     * Get the title (245ab) from a record, without non-filing chars as
     * specified in 245 2nd indicator, and lowercased. 
     * @param record - the marc record object
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
        
        String title = marcMetadataRecord.getTitle().get(0).getTitleStr();
        title = title.toLowerCase();
        
        //Skip non-filing chars, if possible. 
        if (title.length() > nonFilingInt )  {
          title = title.substring(nonFilingInt);          
        }
        
        if ( title.length() == 0) {
          return null;
        }                
        
        return title;
    }

	public String getFullrecord() {
		return marcMetadataRecord.export(IOFormat.ISO_2709);
	}
	
	public List<HarvestedRecordFormatEnum> getRecordType(){
		return marcMetadataRecord.getDetectedFormatList();
	}

	public String isIllustrated() {
		return null; // FIXME
	}
	
	public String getBoundingBox() {
		return null; // FIXME
	}

	public Object methodMissing(String methodName, Object args) {
		RecordFunction<MarcRecord> func = functions.get(methodName);
		if (func == null) {
			throw new IllegalArgumentException(String.format("missing function: %s", methodName));
		}
		return func.apply(record, args);
	}
	
    protected int getInd2AsInt(DataField df) {
        char ind2char = df.getIndicator2();
        int result = 0;
        if (Character.isDigit(ind2char))
            result = Integer.valueOf(String.valueOf(ind2char));
        return result;
    }
    
    public List<String> getPublisher(){
    	List<String> publishers = new ArrayList<String>();
    	for(DataField dataField: record.getDataFields("264")){
    		if(dataField.getIndicator2() == '1'){
    			publishers.addAll(getFieldsTrim("264b"));
    		}
    	}
    	publishers.addAll(getFieldsTrim("260b:928a:978abcdg"));
    	
    	return publishers;
    }

    public Set<String> getFieldsTrim(String tags){
    	Set<String> result = new HashSet<String>();
    	for(String data: getFields(tags)){
    		data = data.replaceAll("[,;:/\\s]+$", "");
    		result.add(data);
    	}
    	return result;
    }
    
    public Set<String> getFieldsUnique(String tags){
    	Set<String> result = new HashSet<String>();
    	for(String data: getFields(tags)){
    		result.add(data);
    	}
    	return result;
    }
 
    public Set<String> getSubject(String tags){
    	Set<String> subjects = new HashSet<String>();

    	for(String subject: getFields(tags)){
    		String up = subject.substring(0,1).toUpperCase() + subject.substring(1);
    		subjects.add(up);
    	}
    	return subjects;
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
    
    public List<String> getCity(){
    	List<String> result = new ArrayList<String>();
    	for(String institution: getFields("910a")){
    		result.add(institution.substring(0, 2));
    	}
    	return result;
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
    
}
