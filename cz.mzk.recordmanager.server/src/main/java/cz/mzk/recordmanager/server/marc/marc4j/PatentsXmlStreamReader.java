package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.ResourceMappingResolver;

public class PatentsXmlStreamReader implements MarcReader{

	@Autowired
	private MarcXmlParser marcXmlParser;
	
	private MappingResolver propertyResolver;
	
    private Record record;

    private MarcFactory factory;
    
    private XMLInputFactory xmlFactory;
    
    private XMLStreamReader xmlReader;
    
    private static final String TEXT_LEADER = "-----nam--22--------450-";
    private static final String TEXT_008_PART1 = "------e";
    private static final String TEXT_008_PART2 = "--------sj----------";
    private static final String TEXT_0242 = "MPT";
    private static final String TEXT_260a = "Praha :";
    private static final String TEXT_260b = "Úřad průmyslového vlastnictví,";
    private static final String TEXT_300a = "1 patent";
    private static final String TEXT_500a_PUBLICATION = "Datum zveřejnění přihlášky: %s";
    private static final String TEXT_500a_APPLICATION = "Datum přihlášení: %s";
    private static final String TEXT_500a_APPL_NUMBER = "Číslo přihlášky: %s";
    private static final String TEXT_500a_DOC_NUMBER = "Číslo dokumentu: %s";
    private static final String TEXT_655a = "patenty";
    private static final String TEXT_0722 = "Konspekt";
    private static final String TEXT_856y = "plný text";
    
    private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";
    
    private static final String ELEMENT_RECORD_EP = "ep-patent-document";
    private static final String ELEMENT_RECORD_CZ = "cz-patent-document";
    private static final String ELEMENT_APPLICANT = "applicant";
    private static final String ELEMENT_INVENTOR = "inventor";
    private static final String ELEMENT_AGENT = "agent";
    private static final String ELEMENT_ORGNAME = "orgname";
    private static final String ELEMENT_LAST_NAME = "last-name";
    private static final String ELEMENT_FIRST_NAME = "first-name";
    private static final String ELEMENT_INVENTION_TITLE = "invention-title";
    private static final String ELEMENT_ABSTRACT = "abstract";
    private static final String ELEMENT_CLASSIFICATION_IPCR = "classification-ipcr";
    private static final String ELEMENT_TEXT = "text";
    private static final String ELEMENT_P = "p";
    private static final String ELEMENT_PUBLICATION_REFERENCE = "publication-reference";
    private static final String ELEMENT_APPLICATION_REFERENCE = "application-reference";
    private static final String ELEMENT_DATE = "date";
    private static final String ELEMENT_DOC_NUMBER = "doc-number";
    
    private static final String ATTRIBUTE_ID = "file";
    private static final String ATTRIBUTE_LANG = "lang";
    private static final String ATTRIBUTE_SEQUENCE = "sequence";
    private static final String ATTRIBUTE_DOC_NUMBER = "doc-number";
    
    private static final String PATENTS_MAP = "patents.map";
    
    private static final Pattern PATTERN_024 = Pattern.compile("(.{4}\\s*\\d+\\/\\d+).*");
    
    private static final Pattern A3_PATTERN = Pattern.compile("St36_CZ_(\\d{4})-(\\d+)_A3");
    private static final String A3_URL = "http://spisy.upv.cz/Applications/%s/PPVCZ%s_%sA3.pdf";
    private static final Pattern B6_PATTERN = Pattern.compile("St36_CZ_(\\d*)_B6");
    private static final String B6_URL = "http://spisy.upv.cz/Patents/FullDocuments/%s/%s.pdf";
    private static final Pattern U1_PATTERN = Pattern.compile("St36_CZ_(\\d*)_U1");
    private static final String U1_URL = "http://spisy.upv.cz/UtilityModels/FullDocuments/FDUM%s/uv%s.pdf";
    
    private static final String DATE_ORIGIN_FORMAT = "yyyyMMdd";
    private static final String DATE_OUTPUT_FORMAT = "d. M. yyyy";
    
    private static final SimpleDateFormat SDF_ORIGIN = new SimpleDateFormat(DATE_ORIGIN_FORMAT);
    private static final SimpleDateFormat SDF_OUTPUT = new SimpleDateFormat(DATE_OUTPUT_FORMAT);
    
    private static final String AUTHOR_NOT_AVAILABLE = "neuveden podle zákona 84/1972 Sb.";
    
    /**
     * Constructs an instance with the specified input stream.
     */
    public PatentsXmlStreamReader(InputStream input) {
    	xmlFactory = XMLInputFactory.newInstance();
        factory = MarcFactoryImpl.newInstance();
        propertyResolver = new ResourceMappingResolver(new ClasspathResourceProvider());
        initializeReader(input);
    }

    private void initializeReader(InputStream input){
    	try {
        	xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			this.xmlReader = xmlFactory.createXMLStreamReader(input);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Returns true if the iteration has more records, false otherwise.
     */
    public boolean hasNext(){
    	try {
			return xmlReader.hasNext();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return false;
    }

    /**
     * Returns the next record in the iteration.
     * 
     * @return Record - the record object
     */
    public Record next() {
        record = null;
        
        DataField df = null;
        String name = null;
        boolean firstAuthor = true; // first to field 100, others 700
        boolean author = true;
        boolean authorAvailable = true;
        boolean firstCorporate = true; // first to field 110, others 710
        boolean b072 = false;
        boolean abstratcs = false;
        boolean appl_reference = false;
        char date = ' ';
        
        try {
			while(xmlReader.hasNext()){
				switch(xmlReader.getEventType()){
				case XMLStreamReader.START_ELEMENT:
					switch(xmlReader.getLocalName()){
					case ELEMENT_RECORD_EP:
					case ELEMENT_RECORD_CZ:
						record = factory.newRecord();
						addFields();
						addIdentifier();					
						addUrl();
						addDocNumber();
						break;
					case ELEMENT_APPLICANT:
					case ELEMENT_INVENTOR:
					case ELEMENT_AGENT:
						author = true;
						break;
					case ELEMENT_ORGNAME:
						name = xmlReader.getElementText();
						author = false;
						break;
					case ELEMENT_FIRST_NAME:
						if(name == null) name = xmlReader.getElementText();
						else name += ", " + xmlReader.getElementText();
						break;
					case ELEMENT_LAST_NAME:
						if(name == null) name = xmlReader.getElementText();
						else name = xmlReader.getElementText() + ", " + name;
						break;
					case ELEMENT_INVENTION_TITLE:
						df = factory.newDataField("TMP", ' ', ' ');
						String a = xmlReader.getAttributeValue(null, ATTRIBUTE_LANG);
						df.addSubfield(factory.newSubfield('a', xmlReader.getElementText()));
						if (a.equalsIgnoreCase("cs")) {
							df.setTag("245");
							if (!authorAvailable) {
								df.addSubfield(factory.newSubfield('c', AUTHOR_NOT_AVAILABLE));
							}
						}
						else {
							df.setTag("246");
							df.setIndicator1('2');
						}
						record.addVariableField(df);
						break;
					case ELEMENT_ABSTRACT:
						if(xmlReader.getAttributeValue(null, ATTRIBUTE_LANG).equalsIgnoreCase("cs")){
							abstratcs = true;
						}
						break;
					case ELEMENT_CLASSIFICATION_IPCR:						
						if(xmlReader.getAttributeValue(null, ATTRIBUTE_SEQUENCE).equals("1")){
							df = factory.newDataField("653", ' ', ' ');
							b072 = true;
						}
						else df = null;
						break;
					case ELEMENT_TEXT:
						String data = xmlReader.getElementText();
						if (b072 && df != null && data.length() >= 4) {
						    String s = data.substring(0, 4);
						    String get = propertyResolver.resolve(PATENTS_MAP).get(s);
						    if (get != null) {
						    	String temp[] = get.split("\\|");
						    	if (temp.length == 2) {
									df.addSubfield(factory.newSubfield('a', temp[0]));
									record.addVariableField(createField072(temp[1]));
						   		}
						   	}
						   	b072 = false;
						}
						addField024(data);
						break;
					case ELEMENT_P:
						if (abstratcs) {
							df = factory.newDataField("520", '3', ' ');
							df.addSubfield(factory.newSubfield('a', xmlReader.getElementText().trim()));
							record.addVariableField(df);
							abstratcs = false;
						}
						break;
					case ELEMENT_PUBLICATION_REFERENCE:
						date = 'p';
						break;
					case ELEMENT_APPLICATION_REFERENCE:
						appl_reference = true;
						date = 'a';
						break;
					case ELEMENT_DATE:
						String dateStr = xmlReader.getElementText();
						if (date == 'p') {
							addField008(dateStr);
							addField260(dateStr);
							addField500aDate(TEXT_500a_PUBLICATION, dateStr);
						}
						else if (date == 'a') {
							addField500aDate(TEXT_500a_APPLICATION, dateStr);
						}
						date = ' ';
						break;
					case ELEMENT_DOC_NUMBER:
						if (appl_reference) {
							add500aApplDocNumber(xmlReader.getElementText());
						}						
						break;
					}	
					break;
				case XMLStreamReader.END_ELEMENT:
					switch(xmlReader.getLocalName()){
					case ELEMENT_APPLICANT:
						if (isAuthorAvailable(name)) {
							addAuthor(author, firstAuthor, firstCorporate, name, "pta");
							if (author) firstAuthor = false;
							else firstCorporate = false;
						}
						else authorAvailable = false;
						name = null;
						break;
					case ELEMENT_INVENTOR:
						if (isAuthorAvailable(name)) {
							addAuthor(author, firstAuthor, firstCorporate, name, "inv");
							if (author) firstAuthor = false;
							else firstCorporate = false;
						}
						else authorAvailable = false;
						name = null;
						break;
					case ELEMENT_AGENT:
						if (isAuthorAvailable(name)) {
							addAuthor(author, firstAuthor, firstCorporate, name, "pth");
							if (author) firstAuthor = false;
							else firstCorporate = false;
						}
						else authorAvailable = false;
						name = null;
						break;
					case ELEMENT_CLASSIFICATION_IPCR:
						if(df != null) record.addVariableField(df);
						break;
					case ELEMENT_RECORD_EP:
					case ELEMENT_RECORD_CZ:
						while(xmlReader.hasNext() && xmlReader.getEventType()!=XMLStreamReader.START_ELEMENT){
							xmlReader.next();
						}
						return sortFields(record);
					case ELEMENT_APPLICATION_REFERENCE:
						appl_reference = false;
						break;
					}
					break;
				}
				xmlReader.next();
				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

        return record;
    }
    
    private boolean isAuthorAvailable(String name) {
    	if (name != null) {
    		return !name.equals(AUTHOR_NOT_AVAILABLE);
		}
    	return true;
    }
    
    private void addAuthor(boolean personalOrCorporate, boolean b100, boolean b110, String name, String utext) {
    	if (name == null) return;
    	String tag = null;
    	if (personalOrCorporate) {
    		if (b100) tag = "100";
        	else tag = "700";
    		name = WordUtils.capitalizeFully(name);
		}
    	else {
    		if (b110) tag = "110";
        	else tag = "710";
		}
		record.addVariableField(factory.newDataField(tag, '1', ' ', "a", name.replaceAll("\\s+", " "), "u", utext));
	}
    
    private void addField500aDate(String text, String date) {
		try {
			record.addVariableField(factory.newDataField("500", ' ', ' ', "a", 
					String.format(text, SDF_OUTPUT.format(SDF_ORIGIN.parse(date)))));
		} catch (ParseException e) {
			e.printStackTrace();
		}
    }
    
    private void add500aApplDocNumber(String data) {
    	if (record.getControlNumber() != null && !record.getControlNumber().endsWith("_A3")) {
    		record.addVariableField(factory.newDataField("500", ' ', ' ', "a", String.format(TEXT_500a_APPL_NUMBER, data)));
		}
	}
    
    private void addField024(String data) {
    	Matcher matcher = PATTERN_024.matcher(data);
    	if (matcher.matches()) {
			record.addVariableField(factory.newDataField("024", '7', ' ', "a", 
					matcher.group(1).replaceAll("\\s+", " "), "2", TEXT_0242));
		}
	}
    
    private void addIdentifier() {
    	record.addVariableField(factory.newControlField("001", xmlReader.getAttributeValue(null, ATTRIBUTE_ID).replace(".xml", "")));
	}
    
    private void addDocNumber() {
    	record.addVariableField(factory.newDataField("500", ' ', ' ', "a", 
    			String.format(TEXT_500a_DOC_NUMBER, xmlReader.getAttributeValue(null, ATTRIBUTE_DOC_NUMBER))));
	}
    
    private void addField008(String date) {
    	if (date != null) {
    		String s008 = TEXT_008_PART1+date+TEXT_008_PART2;
    		record.addVariableField(factory.newControlField("008", s008));
		}
	}
    
    private void addFields() {
    	record.setLeader(factory.newLeader(TEXT_LEADER));

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_005);
		record.addVariableField(factory.newControlField("005", sdf.format(new Date())));

    	record.addVariableField(factory.newDataField("300", ' ', ' ', "a", TEXT_300a));

    	record.addVariableField(factory.newDataField("655", '4', ' ', "a", TEXT_655a));
    }
    
    private void addField260(String date) {
    	if (date != null && date.length() >= 4) {
	    	DataField df = factory.newDataField("260", ' ', ' ');
			df.addSubfield(factory.newSubfield('a', TEXT_260a));
			df.addSubfield(factory.newSubfield('b', TEXT_260b));
			df.addSubfield(factory.newSubfield('c', date.substring(0, 4)));
			record.addVariableField(df);
		}
    }
    
    private DataField createField072(String text) {
		String temp[] = text.split(" - ");
		DataField df = factory.newDataField("072", '7', ' ');
		df.addSubfield(factory.newSubfield('a', temp[0].split(" ")[1]));
		df.addSubfield(factory.newSubfield('x', temp[1]));
		df.addSubfield(factory.newSubfield('2', TEXT_0722));
		df.addSubfield(factory.newSubfield('9', temp[0].split(" ")[0]));

    	return df;
    }
    
    private void addUrl() {
    	String url = null;
    	Matcher matcher = A3_PATTERN.matcher(record.getControlNumber());
		if (matcher.matches()) {
			url = String.format(A3_URL, matcher.group(1), matcher.group(1), StringUtils.leftPad(matcher.group(2), 4, '0'));
		}
		
		matcher = B6_PATTERN.matcher(record.getControlNumber());
		if (matcher.matches()) {
			String dir = null;
			if (matcher.group(1).length() <= 3) dir = "0";
			else {
				dir = matcher.group(1).substring(0, matcher.group(1).length() - 3);
			}
			url = String.format(B6_URL, dir, matcher.group(1));
		}
		
		matcher = U1_PATTERN.matcher(record.getControlNumber());
		if (matcher.matches()) {
			String dir = null;
			if (matcher.group(1).length() <= 3) dir = "0000";
			else {
				dir = StringUtils.leftPad(matcher.group(1).substring(0, matcher.group(1).length() - 3), 4, '0');
			}
			url = String.format(U1_URL, dir, StringUtils.leftPad(matcher.group(1), 6, '0'));
		}
		
		if (url != null) {
			DataField df = factory.newDataField("856", '4', ' ', "u", url, "y", TEXT_856y);
			record.addVariableField(df);
		}
	}

    private Record sortFields(Record record){
    	Record newRecord = factory.newRecord();
		newRecord.setLeader(record.getLeader());
		for(ControlField cf: record.getControlFields()){
			newRecord.addVariableField(cf);
		}
		MarcRecord marc = new MarcRecordImpl(record);
		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for(String tag: new TreeSet<String>(dfMap.keySet())){ // sorted tags
			for(DataField df: dfMap.get(tag)){
				newRecord.addVariableField(df);
			}
		}
		
		return newRecord;
    }

}
