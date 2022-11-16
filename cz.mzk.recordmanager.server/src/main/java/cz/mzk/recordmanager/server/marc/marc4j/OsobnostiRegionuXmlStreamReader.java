package cz.mzk.recordmanager.server.marc.marc4j;

import cz.mzk.recordmanager.server.util.RecordUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OsobnostiRegionuXmlStreamReader implements MarcReader {

	private Record record;

	private final MarcFactory factory;

	private final XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String TEXT_LEADER = "-----nz--a2200000n--4500";
	private static final String TEXT_008_END = "|n|acnnna|bn-----------n-a|a------";
	private static final String TEXT_003_TRE = "CZ-CtMK";
	private static final String TEXT_003_MKUO = "CZ-UoMK";
	private static final String TEXT_040A_TRE = "UOG505";
	private static final String TEXT_040A_MKUO = "UOG001";
	private static final String TEXT_040B = "cze";
	private static final String TEXT_040D = "BOA001";

	private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";
	private static final String DATE_STRING_008 = "yyMMdd";

	private static final String ELEMENT_RECORD = "doc";
	private static final String ELEMENT_FIELD = "field";

	private static final String ATTR_NAME = "name";
	private static final String ATTR_NAME_ID = "id";
	private static final String ATTR_NAME_INSTITUTION = "institution";
	private static final String ATTR_NAME_PRIMARY_SURNAME = "primarySurname";
	private static final String ATTR_NAME_PRIMARY_FIRSTNAME = "primaryFirstname";
	private static final String ATTR_NAME_BIRTHDATE = "birthDate";
	private static final String ATTR_NAME_DEATHDATE = "deathDate";
	private static final String ATTR_NAME_SOURCE = "source";
	private static final String ATTR_NAME_DESCRIPTION = "description";
	private static final String ATTR_NAME_URL = "url";
	private static final String ATTR_NAME_IMG_URL = "previewImageUrl";
	private static final String ATTR_NAME_FIELD = "field";

	private static final String INSTITUTION_TRE = "Městská knihovna Česká Třebová";
	private static final String INSTITUTION_MKUO = "Městská knihovna Ústí nad Orlicí";

	private static final String URL_WOMAN = "https://www.osobnostiregionu.cz/components/com_osobnosti/images/no_photo_woman_fp.png";
	private static final String URL_MAN = "https://www.osobnostiregionu.cz/components/com_osobnosti/images/no_photo_man_fp.png";

	private static final String NEW_LINE = "\n";

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public OsobnostiRegionuXmlStreamReader(InputStream input) {
		xmlFactory = XMLInputFactory.newInstance();
		factory = MarcFactoryImpl.newInstance();
		initializeReader(input);
	}

	private void initializeReader(InputStream input) {
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
	public boolean hasNext() {
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
		List<String> occupations = new ArrayList<>();
		String description = "";
		Person primaryName = new Person();
		try {
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						record = factory.newRecord();
						createFields();
						break;
					case ELEMENT_FIELD:
						switch (xmlReader.getAttributeValue(null, ATTR_NAME)) {
						case ATTR_NAME_ID:
							newControlfield("001", xmlReader.getElementText());
							break;
						case ATTR_NAME_INSTITUTION:
							addFieldsByInstitution(xmlReader.getElementText());
							break;
						case ATTR_NAME_IMG_URL:
						case ATTR_NAME_URL:
							addField856(xmlReader.getElementText());
							break;
						case ATTR_NAME_FIELD:
							occupations.add(xmlReader.getElementText());
							break;
						case ATTR_NAME_DESCRIPTION:
							description = StringEscapeUtils.unescapeHtml4(xmlReader.getElementText());
							break;
						case ATTR_NAME_SOURCE:
							addField670(xmlReader.getElementText());
							break;
						case ATTR_NAME_PRIMARY_FIRSTNAME:
							primaryName.setFirstname(xmlReader.getElementText());
							break;
						case ATTR_NAME_PRIMARY_SURNAME:
							primaryName.setSurname(xmlReader.getElementText());
							break;
						case ATTR_NAME_BIRTHDATE:
							primaryName.setBirth(xmlReader.getElementText());
							break;
						case ATTR_NAME_DEATHDATE:
							primaryName.setDeath(xmlReader.getElementText());
							break;
						}
						break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						while (xmlReader.hasNext() && xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
							xmlReader.next();
						}
						addField678(description);
						addField374(occupations);
						addAuthor(primaryName);
						return RecordUtils.sortFields(record);
					}
					break;
				}
				xmlReader.next();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return record;
	}

	private void addFieldsByInstitution(String institution) {
		if (institution.equals(INSTITUTION_TRE)) addFields003And040(TEXT_003_TRE, TEXT_040A_TRE);
		if (institution.equals(INSTITUTION_MKUO) && record.getVariableFields("040").isEmpty())
			addFields003And040(TEXT_003_MKUO, TEXT_040A_MKUO);
	}

	private void addFields003And040(String f003, String sigla) {
		newControlfield("003", f003);
		DataField df = factory.newDataField("040", ' ', ' ');
		newSubfield(df, 'a', sigla);
		newSubfield(df, 'b', TEXT_040B);
		newSubfield(df, 'd', TEXT_040D);
		record.addVariableField(df);

	}

	private void addField678(String description) {
		if (!description.isEmpty()) {
			DataField df = factory.newDataField("678", '0', ' ');
			newSubfield(df, 'a', description.trim());
			record.addVariableField(df);
		}
	}

	private void addField374(List<String> occupations) {
		if (occupations.isEmpty()) return;
		DataField df = factory.newDataField("374", ' ', ' ');
		for (String occupation : occupations) {
			newSubfield(df, 'a', occupation.trim());
		}
		record.addVariableField(df);
	}

	private void createFields() {
		record.setLeader(factory.newLeader(TEXT_LEADER));

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_005);
		newControlfield("005", sdf.format(new Date()));
		sdf = new SimpleDateFormat(DATE_STRING_008);
		newControlfield("008", sdf.format(new Date()) + TEXT_008_END);
	}

	private void addField856(String url) {
		if (url.equals(URL_MAN) || url.equals(URL_WOMAN)) return;
		DataField df = factory.newDataField("856", '4', ' ');
		newSubfield(df, 'u', url);
		record.addVariableField(df);
	}

	private void addField670(String sources) {
		for (String source : sources.split(NEW_LINE)) {
			DataField df = factory.newDataField("670", ' ', ' ');
			newSubfield(df, 'a', source);
			record.addVariableField(df);
		}
	}

	private void addAuthor(Person name) {
		DataField df = factory.newDataField("100", '1', ' ');
		if (name.getName() != null) newSubfield(df, 'a', name.getName());
		if (!name.getDate().isEmpty()) newSubfield(df, 'd', name.getDate());
		record.addVariableField(df);
	}

	private void newControlfield(String tag, String value) {
		record.addVariableField(factory.newControlField(tag, value));
	}

	private void newSubfield(DataField df, char code, String data) {
		df.addSubfield(factory.newSubfield(code, data));
	}

	private class Person {

		String firstname;
		String surname;
		String birth = "";
		String death = "";

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		public void setSurname(String surname) {
			this.surname = surname;
		}

		public String getName(){
			if((surname == null) && (firstname == null)) return null;
			if(surname == null) return firstname;
			if(firstname == null) return surname;
			return surname + ", " + firstname;
		}
		
		public String getDate() {
			if(birth.isEmpty() && death.isEmpty()) return "";
			return birth+ '-' +death;
		}
		
		public void setBirth(String birth) {
			if(birth.length() > 3) this.birth = birth.substring(0, 4);
		}
		
		public void setDeath(String death) {
			if(death.length() > 3) this.death = death.substring(0, 4);
		}
	}
}
