package cz.mzk.recordmanager.server.marc.marc4j;

import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZakonyProLidiMetadataXmlStreamReader implements MarcReader {

	private Record record;

	private MarcFactory factory;

	private XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_RECORD = "DocInfo";

	private static final String ATTR_NAME_CODE = "Code";
	private static final String ATTR_NAME_PUBLISH_DATE = "PublishDate";
	private static final String ATTR_NAME_QUOTE = "Quote";
	private static final String ATTR_NAME_TITLE = "Title";
	private static final String ATTR_NAME_EFFECT_FROM = "EffectFrom";
	private static final String ATTR_NAME_EFFECT_TILL = "EffectTill";
	private static final String ATTR_NAME_HREF = "Href";
	private static final String ATTR_NAME_DOC_TYPE = "DocType";

	private static final String TEXT_LEADER = "-----nam--22--------450-";
	private static final String TEXT_007 = "ta";
	private static final String TEXT_008 = "------e%sxr------------------cze";
	private static final String TEXT_110A = "Česko";
	private static final String TEXT_1107 = "ge128065";
	private static final String TEXT_245 = "%s č. %s%s";
	private static final String TEXT_300A = "elektronický zdroj";
	private static final String TEXT_500A_FROM = "Účinnost od %s";
	private static final String TEXT_500A_TILL = "Účinnost do %s";
	private static final String TEXT_655A_NAR_VLADY = "nařízení vlády";
	private static final String TEXT_6557_NAR_VLADY = "fd187880";
	private static final String TEXT_655A_PRAVIDLA = "pravidla";
	private static final String TEXT_6557_PRAVIDLA = "fd133121";
	private static final String TEXT_655A_SMERNICE = "směrnice";
	private static final String TEXT_6557_SMERNICE = "fd133465";
	private static final String TEXT_655A_SMLOUVY = "smlouvy";
	private static final String TEXT_6557_SMLOUVY = "fd187886";
	private static final String TEXT_655A_STANOVY = "stanovy";
	private static final String TEXT_6557_STANOVY = "fd133584";
	private static final String TEXT_655A_STANOVY_EN = "statutes";
	private static final String TEXT_655A_USNESENI = "usnesení vlády";
	private static final String TEXT_6557_USNESENI = "fd185245";
	private static final String TEXT_655A_VYHLASKY = "vyhlášky";
	private static final String TEXT_6557_VYHLASKY = "fd185965";
	private static final String TEXT_655A_VYHLASKY_EN = "promulgations";
	private static final String TEXT_655A_ZAKONY = "zákony";
	private static final String TEXT_6557_ZAKONY = "fd133886";
	private static final String TEXT_655A_ZAKONY_EN = "laws";
	private static final String TEXT_6552_CZE = "czenas";
	private static final String TEXT_6552_EN = "ennas";
	private static final String TEXT_856U = "http://www.zakonyprolidi.cz%s";
	public static final String TEXT_856Y = "fulltext_link";

	private static final String TYPE_NARIZENI_VLADY = "Narizeni_vlady";
	private static final String TYPE_VYHLASKA = "Vyhlaska";
	private static final String TYPE_ZAKON = "Zakon";
	private static final String TYPE_UPLNE_ZNENI = "Uplne_zneni";
	private static final String TYPE_USTAVA = "Ustava";
	private static final String TYPE_USNESENI = "Usneseni";
	private static final String TYPE_SMERNICE = "Smernice";
	private static final String TYPE_SMLOUVY = "Smlouvy";
	private static final String TYPE_PRAVIDLA = "Pravidla";
	private static final String TYPE_STANOVY = "Stanovy";

	private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";
	private static final String DATE_ORIGIN_FORMAT = "yyyy-MM-dd";
	private static final String DATE_STRING_008 = "yyyyMMdd";
	private static final String DATE_STRING_260 = "yyyy";
	private static final String DATE_STRING_500 = "d. M. yyyy";

	private static final SimpleDateFormat SDF_ORIGIN = new SimpleDateFormat(DATE_ORIGIN_FORMAT);
	private static final SimpleDateFormat SDF_005 = new SimpleDateFormat(DATE_STRING_005);
	private static final SimpleDateFormat SDF_008 = new SimpleDateFormat(DATE_STRING_008);
	private static final SimpleDateFormat SDF_260 = new SimpleDateFormat(DATE_STRING_260);
	private static final SimpleDateFormat SDF_500 = new SimpleDateFormat(DATE_STRING_500);

	private static final Pattern PATERN245 = Pattern.compile("(?i)((?:[^\\s]+\\s)?(?:vyhláška|"
			+ "zákon|nález ústavního soudu|nařízení vlády|vládní nařízení|sdělení|rozhodnutí))(.*)");

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public ZakonyProLidiMetadataXmlStreamReader(InputStream input) {
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

	@Override
	public boolean hasNext() {
		try {
			return xmlReader.hasNext();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Record next() {
		record = null;

		try {
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						record = factory.newRecord();
						record.setLeader(factory.newLeader(TEXT_LEADER));
						newControlfield("001", getAttr(ATTR_NAME_CODE));
						newControlfield("005", SDF_005.format(new Date()));
						newControlfield("007", TEXT_007);
						newControlfield("008", String.format(TEXT_008, SDF_008.format(SDF_ORIGIN.parse(getAttr(ATTR_NAME_PUBLISH_DATE)))));
						addField110();
						addField245();
						addField260();
						addField300();
						addField500(TEXT_500A_FROM, getAttr(ATTR_NAME_EFFECT_FROM));
						addField500(TEXT_500A_TILL, getAttr(ATTR_NAME_EFFECT_TILL));
						addField653();
						addField655();
						addField856();
						break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						while (xmlReader.hasNext() && xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
							xmlReader.next();
						}
						return record;
					}
					break;
				}
				xmlReader.next();

			}
		} catch (XMLStreamException | ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String getAttr(String attributeName) {
		return xmlReader.getAttributeValue(null, attributeName);
	}

	private void newControlfield(String tag, String value) {
		record.addVariableField(factory.newControlField(tag, value));
	}

	private void newSubfield(DataField df, char code, String data) {
		df.addSubfield(factory.newSubfield(code, data));
	}

	private void addField110() {
		DataField df = factory.newDataField("110", '1', ' ');
		newSubfield(df, 'a', TEXT_110A);
		newSubfield(df, '7', TEXT_1107);
		record.addVariableField(df);
	}

	private void addField245() {
		DataField df = factory.newDataField("245", '0', '0');

		Matcher matcher = PATERN245.matcher(getAttr(ATTR_NAME_TITLE).trim());
		if (matcher.matches()) {
			newSubfield(df, 'a', String.format(TEXT_245, matcher.group(1), getAttr(ATTR_NAME_QUOTE), matcher.group(2)));
		} else newSubfield(df, 'a', getAttr(ATTR_NAME_TITLE));

		record.addVariableField(df);
	}

	private void addField260() throws ParseException {
		DataField df = factory.newDataField("260", ' ', ' ');
		newSubfield(df, 'c', SDF_260.format(SDF_ORIGIN.parse(getAttr(ATTR_NAME_PUBLISH_DATE))));
		record.addVariableField(df);
	}

	private void addField300() {
		DataField df = factory.newDataField("300", ' ', ' ');
		newSubfield(df, 'a', TEXT_300A);
		record.addVariableField(df);
	}

	private void addField500(String text, String data) throws ParseException {
		if (data == null) return;
		DataField df = factory.newDataField("500", ' ', ' ');
		newSubfield(df, 'a', String.format(text, SDF_500.format(SDF_ORIGIN.parse(data))));
		record.addVariableField(df);
	}

	private void addField653() {
		DataField df = factory.newDataField("653", '1', '6');
		newSubfield(df, 'a', getAttr(ATTR_NAME_DOC_TYPE));
		record.addVariableField(df);
	}

	private void addField655() {
		switch (getAttr(ATTR_NAME_DOC_TYPE)) {
		case TYPE_NARIZENI_VLADY:
			addField6557(TEXT_655A_NAR_VLADY, TEXT_6557_NAR_VLADY, TEXT_6552_CZE);
			break;
		case TYPE_VYHLASKA:
			addField6557(TEXT_655A_VYHLASKY, TEXT_6557_VYHLASKY, TEXT_6552_CZE);
			addField6559(TEXT_655A_VYHLASKY_EN, TEXT_6552_EN);
			break;
		case TYPE_ZAKON:
		case TYPE_UPLNE_ZNENI:
		case TYPE_USTAVA:
			addField6557(TEXT_655A_ZAKONY, TEXT_6557_ZAKONY, TEXT_6552_CZE);
			addField6559(TEXT_655A_ZAKONY_EN, TEXT_6552_EN);
			break;
		case TYPE_USNESENI:
			addField6557(TEXT_655A_USNESENI, TEXT_6557_USNESENI, TEXT_6552_CZE);
			break;
		case TYPE_SMERNICE:
			addField6557(TEXT_655A_SMERNICE, TEXT_6557_SMERNICE, TEXT_6552_CZE);
			break;
		case TYPE_SMLOUVY:
			addField6557(TEXT_655A_SMLOUVY, TEXT_6557_SMLOUVY, TEXT_6552_CZE);
			break;
		case TYPE_PRAVIDLA:
			addField6557(TEXT_655A_PRAVIDLA, TEXT_6557_PRAVIDLA, TEXT_6552_CZE);
			break;
		case TYPE_STANOVY:
			addField6557(TEXT_655A_STANOVY, TEXT_6557_STANOVY, TEXT_6552_CZE);
			addField6559(TEXT_655A_STANOVY_EN, TEXT_6552_EN);
			break;
		default:
			break;
		}
	}

	private void addField6557(String textA, String text7, String text2) {
		DataField df = factory.newDataField("655", '7', ' ');
		newSubfield(df, 'a', textA);
		newSubfield(df, '7', text7);
		newSubfield(df, '2', text2);
		record.addVariableField(df);
	}

	private void addField6559(String textA, String text2) {
		DataField df = factory.newDataField("655", '9', ' ');
		newSubfield(df, 'a', textA);
		newSubfield(df, '2', text2);
		record.addVariableField(df);
	}

	private void addField856() {
		DataField df = factory.newDataField("856", '4', '1');
		newSubfield(df, 'u', String.format(TEXT_856U, getAttr(ATTR_NAME_HREF)));
		newSubfield(df, 'y', TEXT_856Y);
		record.addVariableField(df);
	}
}
