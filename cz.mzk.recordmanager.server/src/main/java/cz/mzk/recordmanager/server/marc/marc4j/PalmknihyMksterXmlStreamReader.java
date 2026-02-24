package cz.mzk.recordmanager.server.marc.marc4j;

import cz.mzk.recordmanager.server.util.RecordUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PalmknihyMksterXmlStreamReader implements MarcReader {
	private final MarcFactory factory;

	private final XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_PALMKNIHY_URL = "OAI";
	private static final String ELEMENT_CATALOGUE_ID = "url";
	private static final String ELEMENT_RECORD = "ebook";

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public PalmknihyMksterXmlStreamReader(InputStream input) {
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
							case ELEMENT_PALMKNIHY_URL:
								record.addVariableField(factory.newDataField("856", '4', '2', "u",
										"https://www.palmknihy.cz/kniha/" + xmlReader.getElementText()));
								break;
							case ELEMENT_CATALOGUE_ID:
								String url = xmlReader.getElementText();
								Matcher matcher = NUMBER_PATTERN.matcher(url);
								if (matcher.find()) {
									record.addVariableField(factory.newControlField("001", matcher.group()));
								}
								break;
						}
						break;
					case XMLStreamReader.END_ELEMENT:
						if (xmlReader.getLocalName().equals(ELEMENT_RECORD)) {
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
