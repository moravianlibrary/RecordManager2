package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class KramAvailabilityXmlStreamReader {

	private XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_DOC = "doc";
	private static final String ELEMENT_STR = "str";
	private static final String ELEMENT_BOOL = "bool";

	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_VALUE_PID = "PID";
	private static final String ATTRIBUTE_VALUE_DOSTUPNOST = "dostupnost";
	private static final String ATTRIBUTE_VALUE_DNNT = "dnnt";

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public KramAvailabilityXmlStreamReader(InputStream input) {
		xmlFactory = XMLInputFactory.newInstance();
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
	public KramAvailability next() {
		KramAvailability result = null;
		try {
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
					case XMLStreamReader.START_ELEMENT:
						if (ELEMENT_DOC.equals(xmlReader.getLocalName())) {
							result = new KramAvailability();
						}
						if (ELEMENT_STR.equals(xmlReader.getLocalName())) {
							if (ATTRIBUTE_VALUE_PID.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
								result.setUuid(xmlReader.getElementText());
							} else if (ATTRIBUTE_VALUE_DOSTUPNOST.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
								result.setAvailability(xmlReader.getElementText());
							}
						}
						if (ELEMENT_BOOL.equals(xmlReader.getLocalName())) {
							if (ATTRIBUTE_VALUE_DNNT.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
								result.setDnnt(xmlReader.getElementText().equals("true"));
							}
						}
						break;
					case XMLStreamReader.END_ELEMENT:
						if (ELEMENT_DOC.equals(xmlReader.getLocalName())) {
							while (xmlReader.hasNext() && xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
								xmlReader.next();
							}
							return result;
						}
						break;
				}
				xmlReader.next();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		return result;
	}

}
