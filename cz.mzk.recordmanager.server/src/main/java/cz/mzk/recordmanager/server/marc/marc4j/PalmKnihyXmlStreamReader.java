package cz.mzk.recordmanager.server.marc.marc4j;

import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.util.RecordUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.regex.Matcher;

public class PalmKnihyXmlStreamReader implements MarcReader {


	private final MarcFactory factory;

	private final XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_ISBN = "isbn";
	private static final String ELEMENT_URL = "shopURL";
	private static final String ELEMENT_VYPUJCKA = "rents";
	private static final String ELEMENT_FORMAT = "format";
	private static final String ELEMENT_PRICE = "price";
	private static final String ELEMENT_SAMPLE_URL = "sampleURL";

	private static final String ATTR_TYPE = "type";

	private static final String ELEMENT_RECORD = "product";

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public PalmKnihyXmlStreamReader(InputStream input) {
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
		Record record = null;
		try {
			String element_format = null;
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						record = factory.newRecord();
						record.addVariableField(factory.newDataField("TYP", ' ', ' ', "a", xmlReader.getAttributeValue(null, "type")));
						element_format = null;
						break;
					case ELEMENT_ISBN:
						if (element_format != null && element_format.equals("pap")) {
							record.addVariableField(factory.newDataField("020", ' ', ' ', "a", xmlReader.getElementText()));
						}
						break;
					case ELEMENT_URL:
						String url = xmlReader.getElementText();
						record.addVariableField(factory.newDataField("856", ' ', ' ', "u", url));
						Matcher matcher = MetadataMarcRecord.PALMKNIHY_ID.matcher(url);
						if (matcher.matches()) {
							record.addVariableField(factory.newDataField("URL", ' ', ' ', "a", matcher.group(1)));
							record.addVariableField(factory.newControlField("001", matcher.group(1)));
						}
						break;
					case ELEMENT_VYPUJCKA:
						String data = xmlReader.getElementText();
						record.addVariableField(factory.newDataField("VYP", ' ', ' ', "a", data.equals("true") ? "1" : "0"));
						break;
					case ELEMENT_FORMAT:
						element_format = xmlReader.getAttributeValue(null, ATTR_TYPE);
						break;
					case ELEMENT_SAMPLE_URL:
						if (element_format != null) {
							record.addVariableField(factory.newDataField("856", ' ', ' ', "u", xmlReader.getElementText(),
									"y", element_format));

						}
						break;
					case ELEMENT_PRICE:
						record.addVariableField(factory.newDataField("PRI", ' ', ' ', "a", xmlReader.getElementText()));
					break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_FORMAT:
						element_format = null;
						break;
					case ELEMENT_RECORD:
						while (xmlReader.hasNext() && xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
							xmlReader.next();
						}
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

}
