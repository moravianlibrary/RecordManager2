package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KramAvailabilityXmlStreamReader {

	private final XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_DOC = "doc";
	private static final String ELEMENT_STR = "str";
	private static final String ELEMENT_BOOL = "bool";
	private static final String ELEMENT_INT = "int";
	private static final String ELEMENT_ARR = "arr";

	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_VALUE_PID = "PID";
	private static final String ATTRIBUTE_VALUE_PARENT_PID = "parent_pid";
	private static final String ATTRIBUTE_VALUE_DOSTUPNOST = "dostupnost";
	private static final String ATTRIBUTE_VALUE_DNNT = "dnnt";
	private static final String ATTRIBUTE_VALUE_LEVEL = "level";
	private static final String ATTRIBUTE_VALUE_DNNT_LABELS = "dnnt-labels";
	private static final String ATTRIBUTE_VALUE_ISSN = "issn";
	private static final String ATTRIBUTE_VALUE_YEAR = "rok";
	private static final String ATTRIBUTE_VALUE_DETAILS = "details";
	private static final String ATTRIBUTE_VALUE_ISSUE = "issue";
	private static final String ATTRIBUTE_VALUE_TITLE = "title";
	private static final String ATTRIBUTE_VALUE_DOCUMENT_TYPE = "document_type";

	private static final Pattern VOLUME = Pattern.compile("\\d+#+(\\d+)");
	private static final Pattern ISSUE = Pattern.compile("#+(\\d+)$");
	private static final Pattern PAGE = Pattern.compile("(\\d+)");

	private static final List<String> ARR_VALUES = Arrays.asList(
			ATTRIBUTE_VALUE_DNNT_LABELS,
			ATTRIBUTE_VALUE_PARENT_PID,
			ATTRIBUTE_VALUE_DETAILS,
			ATTRIBUTE_VALUE_ISSUE,
			ATTRIBUTE_VALUE_DOCUMENT_TYPE
	);

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
			String arrName = "";
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					if (ELEMENT_DOC.equals(xmlReader.getLocalName())) {
						result = new KramAvailability();
					}
					if (ELEMENT_STR.equals(xmlReader.getLocalName())) {
						if (arrName.equals(ATTRIBUTE_VALUE_DNNT_LABELS)) {
							result.addDnntLabel(xmlReader.getElementText());
						} else if (arrName.equals(ATTRIBUTE_VALUE_PARENT_PID)) {
							result.setParentUuid(xmlReader.getElementText());
						} else if (arrName.equals(ATTRIBUTE_VALUE_DETAILS)) {
							String text = xmlReader.getElementText();
							Matcher matcher = VOLUME.matcher(text);
							if (matcher.find()) {
								result.setVolume(parseInt(matcher.group(1)));
							}
							matcher = ISSUE.matcher(text);
							if (matcher.find()) {
								result.setIssue(parseInt(matcher.group(1)));
							}
						} else if (arrName.equals(ATTRIBUTE_VALUE_DOCUMENT_TYPE)) {
							result.setType(xmlReader.getElementText());
						} else if (ATTRIBUTE_VALUE_PID.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setUuid(xmlReader.getElementText());
						} else if (ATTRIBUTE_VALUE_DOSTUPNOST.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setAvailability(xmlReader.getElementText());
						} else if (ATTRIBUTE_VALUE_ISSN.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setIssn(xmlReader.getElementText());
						} else if (ATTRIBUTE_VALUE_TITLE.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							Matcher matcher = PAGE.matcher(xmlReader.getElementText());
							if (matcher.find()) result.setPage(parseInt(matcher.group(1)));
						}
					}
					if (ELEMENT_BOOL.equals(xmlReader.getLocalName())) {
						if (ATTRIBUTE_VALUE_DNNT.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setDnnt(xmlReader.getElementText().equals("true"));
						}
					}
					if (ELEMENT_INT.equals(xmlReader.getLocalName())) {
						if (ATTRIBUTE_VALUE_LEVEL.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setLevel(parseInt(xmlReader.getElementText()));
						} else if (ATTRIBUTE_VALUE_YEAR.equals(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							result.setYaer(parseInt(xmlReader.getElementText()));
						}
					}
					if (ELEMENT_ARR.equals(xmlReader.getLocalName())) {
						if (ARR_VALUES.contains(xmlReader.getAttributeValue(null, ATTRIBUTE_NAME))) {
							arrName = xmlReader.getAttributeValue(null, ATTRIBUTE_NAME);
						}
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					if (ELEMENT_ARR.equals(xmlReader.getLocalName())) arrName = "";
					if (ELEMENT_DOC.equals(xmlReader.getLocalName())) {
						while (xmlReader.hasNext() && xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
							xmlReader.next();
						}
						return parse(result);
					}
					break;
				}
				xmlReader.next();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		return parse(result);
	}

	private static KramAvailability parse(KramAvailability availability) {
		if (availability == null) return null;
		if (availability.getType().equals("periodicalitem")) availability.setVolume(null);
		if (availability.getType().equals("periodicalvolume")) availability.setIssue(null);
		return availability;
	}

	private static Integer parseInt(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException nfr) {
			return null;
		}
	}

}
