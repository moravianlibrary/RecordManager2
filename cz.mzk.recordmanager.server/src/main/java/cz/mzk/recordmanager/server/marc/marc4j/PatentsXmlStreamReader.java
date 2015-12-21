package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
    private static final String TEXT_007 = "ta";
    private static final String TEXT_008_PART1 = "------e";
    private static final String TEXT_008_PART2 = "--------sj----------cze";
    private static final String TEXT_260a = "Praha";
    private static final String TEXT_260b = "Úřad průmyslového vlastnictví";
    private static final String TEXT_300a = "elektronický zdroj";
    
    private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";
    
    private static final String ELEMENT_RECORD = "cz-patent-document";
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
    
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_DATE_PUBLISHED = "date-published";
    private static final String ATTRIBUTE_LANG = "lang";
    private static final String ATTRIBUTE_SEQUENCE = "sequence";
    
    private static final String PATENTS_MAP = "patents.map";
    
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
        boolean b100 = false; // first to field 100, others 700 
        boolean b110 = false; // first to field 110, others 710
        boolean shouldByProcessedText = false;
        
        try {
			while(xmlReader.hasNext()){
				switch(xmlReader.getEventType()){
				case XMLStreamReader.START_ELEMENT:
					switch(xmlReader.getLocalName()){
					case ELEMENT_RECORD:
						record = factory.newRecord();
						createFields(record);
						record.addVariableField(factory.newControlField("001", xmlReader.getAttributeValue(null, ATTRIBUTE_ID)));
						String s008 = TEXT_008_PART1+xmlReader.getAttributeValue(null, ATTRIBUTE_DATE_PUBLISHED)+TEXT_008_PART2;
						record.addVariableField(factory.newControlField("008", s008));
						record.addVariableField(createField260());					
						break;
					case ELEMENT_APPLICANT:
					case ELEMENT_INVENTOR:
					case ELEMENT_AGENT:
						df = factory.newDataField();
						df.setIndicator1('1');
						df.setIndicator2(' ');
						break;
					case ELEMENT_ORGNAME:
						if(b110) df.setTag("710");
						else df.setTag("110");
						df.addSubfield(factory.newSubfield('a', xmlReader.getElementText()));
						break;
					case ELEMENT_FIRST_NAME:
						if(name == null) name = xmlReader.getElementText();
						else{
							df.addSubfield(factory.newSubfield('a', name+", "+xmlReader.getElementText()));
							b100 = true;
							name = null;
						}
						break;
					case ELEMENT_LAST_NAME:
						if(b100) df.setTag("700");
						else df.setTag("100");
						if(name == null) name = xmlReader.getElementText();
						else{
							df.addSubfield(factory.newSubfield('a', xmlReader.getElementText()+", "+name));
							name = null;
							b100 = true;
						}
						break;
					case ELEMENT_INVENTION_TITLE:						
						if(xmlReader.getAttributeValue(null, ATTRIBUTE_LANG).equals("cs")){
							df = factory.newDataField("245", ' ', ' ');					
						}
						else{
							df = factory.newDataField("246", '2', ' ');
						}
						df.addSubfield(factory.newSubfield('a', xmlReader.getElementText()));
						record.addVariableField(df);
						break;
					case ELEMENT_ABSTRACT:
						if(xmlReader.getAttributeValue(null, ATTRIBUTE_LANG).equals("cs")){
							df = factory.newDataField("520", '3', ' ');
							df.addSubfield(factory.newSubfield('a', xmlReader.getElementText().trim()));
							record.addVariableField(df);
						}
						break;
					case ELEMENT_CLASSIFICATION_IPCR:						
						if(xmlReader.getAttributeValue(null, ATTRIBUTE_SEQUENCE).equals("1")){
							df = factory.newDataField("653", ' ', ' ');
							shouldByProcessedText = true;
						}
						else df = null;
						break;
					case ELEMENT_TEXT:
						if(shouldByProcessedText){
							String s = xmlReader.getElementText().substring(0, 4);
							df.addSubfield(factory.newSubfield('a', propertyResolver.resolve(PATENTS_MAP).get(s)));
							shouldByProcessedText = false;
						}
						break;
					}	
					break;			
				case XMLStreamReader.END_ELEMENT:
					switch(xmlReader.getLocalName()){
					case ELEMENT_APPLICANT:
						df.addSubfield(factory.newSubfield('u', "pta"));
						record.addVariableField(df);
						break;
					case ELEMENT_INVENTOR:
						df.addSubfield(factory.newSubfield('u', "inv"));
						record.addVariableField(df);
						break;
					case ELEMENT_AGENT:
						df.addSubfield(factory.newSubfield('u', "pth"));
						record.addVariableField(df);
						break;
					case ELEMENT_CLASSIFICATION_IPCR:
						if(df != null) record.addVariableField(df);
						break;
					case ELEMENT_RECORD:
						while(xmlReader.hasNext() && xmlReader.getEventType()!=XMLStreamReader.START_ELEMENT){
							xmlReader.next();
						}
						return sortFields(record);
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
    
    private void createFields(Record record){
    	record.setLeader(factory.newLeader(TEXT_LEADER));

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_005);
		record.addVariableField(factory.newControlField("005", sdf.format(new Date())));

    	record.addVariableField(factory.newControlField("007", TEXT_007));
    	
    	DataField df = factory.newDataField("300", ' ', ' ');
    	df.addSubfield(factory.newSubfield('a', TEXT_300a));
    	record.addVariableField(df);
    }
    
    private DataField createField260(){
    	DataField df = factory.newDataField("260", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', TEXT_260a));
		df.addSubfield(factory.newSubfield('b', TEXT_260b));
		String s260c = xmlReader.getAttributeValue(null, ATTRIBUTE_DATE_PUBLISHED).substring(0, 4);
		df.addSubfield(factory.newSubfield('c', s260c));
		return df;
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
