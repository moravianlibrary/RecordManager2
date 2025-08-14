package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;
import cz.mzk.recordmanager.server.model.KramAvailability;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

import static cz.mzk.recordmanager.server.kramerius.ApiMappingEnum.*;

public class KramAvailabilityXmlStreamReader {

	private XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_DOC = "doc";
	private static final String ELEMENT_STR = "str";
	private static final String ELEMENT_INT = "int";
	private static final String ELEMENT_ARR = "arr";

	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_VALUE_LEVEL = "level";

	private final KrameriusHarvesterParams params;

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public KramAvailabilityXmlStreamReader(InputStream input, KrameriusHarvesterParams params) {
		this.params = params;
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
			String arrName = "";
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					if (ELEMENT_DOC.equals(xmlReader.getLocalName())) {
						result = new KramAvailability();
					}
					if (ELEMENT_STR.equals(xmlReader.getLocalName())) {
						if (arrName.equals(params.getApiMappingValue(DNNT_LABELS)) || arrName.equals(params.getApiMappingValue(LICENSES))) {
							result.addDnntLabel(xmlReader.getElementText());
						} else if (params.getApiMappingValue(PID).equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setUuid(xmlReader.getElementText());
						} else if (params.getApiMappingValue(ACCESSIBILITY).equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setAvailability(xmlReader.getElementText());
						}

					}
					if (ELEMENT_INT.equals(xmlReader.getLocalName())) {
						if (ATTRIBUTE_VALUE_LEVEL.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							try {
								result.setLevel(Integer.parseInt(xmlReader.getElementText()));
							} catch (NumberFormatException nfr) {
								result.setLevel(null);
							}
						}
					}
					if (ELEMENT_ARR.equals(xmlReader.getLocalName())) {
						String value = xmlReader.getAttributeValue(null, ATTRIBUTE_NAME);
						if (params.getApiMappingValue(DNNT_LABELS).equals(value)
								|| params.getApiMappingValue(LICENSES).equals(value)) {
							arrName = params.getApiMappingValue(DNNT_LABELS);
						}
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					if (ELEMENT_ARR.equals(xmlReader.getLocalName())) arrName = "";
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
