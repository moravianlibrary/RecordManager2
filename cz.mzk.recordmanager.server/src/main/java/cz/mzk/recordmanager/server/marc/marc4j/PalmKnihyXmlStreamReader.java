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

	private static final String ELEMENT_ISBN = "ISBN_TISTENE";
	private static final String ELEMENT_ID = "ID";
	private static final String ELEMENT_URL = "URL";
	private static final String ELEMENT_VYPUJCKA = "VYPUJCKA";

	private static final String ELEMENT_RECORD = "EBOOK";

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
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						record = factory.newRecord();
						break;
					case ELEMENT_ID:
						record.addVariableField(factory.newControlField("001", xmlReader.getElementText()));
						break;
					case ELEMENT_ISBN:
						record.addVariableField(factory.newDataField("020", ' ', ' ', "a", xmlReader.getElementText()));
						break;
					case ELEMENT_URL:
						String url = xmlReader.getElementText();
						record.addVariableField(factory.newDataField("856", ' ', ' ', "u", url));
						Matcher matcher = MetadataMarcRecord.PALMKNIHY_ID.matcher(url);
						if (matcher.matches())
							record.addVariableField(factory.newDataField("URL", ' ', ' ', "a", matcher.group(1)));
						break;
					case ELEMENT_VYPUJCKA:
						record.addVariableField(factory.newDataField("VYP", ' ', ' ', "a", xmlReader.getElementText()));
						break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xmlReader.getLocalName()) {
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
