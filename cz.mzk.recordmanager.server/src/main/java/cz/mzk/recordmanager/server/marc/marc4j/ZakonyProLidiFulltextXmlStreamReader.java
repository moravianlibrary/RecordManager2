package cz.mzk.recordmanager.server.marc.marc4j;

import com.ctc.wstx.exc.WstxEOFException;
import com.ctc.wstx.exc.WstxParsingException;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.XMLEventReaderUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

public class ZakonyProLidiFulltextXmlStreamReader {

	private XMLInputFactory xmlFactory;

	private XMLEventReader eventReader;

	private static final String ELEMENT_DIV = "div";
	private static final String ELEMENT_P = "p";
	private static final String ELEMENT_H3 = "h3";

	private static final String ATTR_NAME_CLASS = "class";

	private static final String ATTR_VALUE_FRAGS_NOVEL = "Frags";

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public ZakonyProLidiFulltextXmlStreamReader(InputStream input) {
		xmlFactory = XMLInputFactory.newInstance();
		initializeReader(input);
	}

	private void initializeReader(InputStream input) {
		try {
			xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			this.eventReader = xmlFactory.createXMLEventReader(input);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public String next() {
		boolean fulltext = false;
		StringBuilder result = new StringBuilder();
		int countDiv = 0;
		while (eventReader.hasNext()) {
			try {
				XMLEvent xmlEvent = eventReader.nextEvent();

				if (xmlEvent.isStartElement()) {
					switch (xmlEvent.asStartElement().getName().getLocalPart()) {
					case ELEMENT_DIV:
						if (isAttribute(xmlEvent.asStartElement(), ATTR_NAME_CLASS, ATTR_VALUE_FRAGS_NOVEL)) {
							fulltext = true;
						}
						if (fulltext) ++countDiv;
						break;
					case ELEMENT_H3:
					case ELEMENT_P:
						if (fulltext) {
							String xmlFragment = readElementBody(eventReader);
							result.append(xmlFragment).append('\n');
						}
						break;
					default:
						break;
					}
				} else if (xmlEvent.isEndElement()) {
					switch (xmlEvent.asEndElement().getName().getLocalPart()) {
					case ELEMENT_DIV:
						if (fulltext) --countDiv;
						if (countDiv == 0) fulltext = false;
						break;
					default:
						break;
					}
				}
			} catch (WstxEOFException e) {
				break;
			} catch (WstxParsingException ignore) {
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	private static final Pattern REPLACE1 = Pattern.compile("<[^>]*>");
	private static final Pattern REPLACE2 = Pattern.compile("^[\\s§0-9]*$");

	private static String readElementBody(final XMLEventReader eventReader) throws XMLStreamException {
		String result = XMLEventReaderUtils.readElementBody(eventReader);
		result = CleaningUtils.replaceAll(result, REPLACE1, "");
		return CleaningUtils.replaceAll(result, REPLACE2, "");
	}

	private static boolean isAttribute(final StartElement element, final String name, final String value) {
		Iterator<?> iterator = element.getAttributes();
		while (iterator.hasNext()) {
			Attribute attribute = (Attribute) iterator.next();
			if (attribute.getName().getLocalPart().equals(name) && attribute.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}
}
