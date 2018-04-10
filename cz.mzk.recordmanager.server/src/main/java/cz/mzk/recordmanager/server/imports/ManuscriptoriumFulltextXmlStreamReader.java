package cz.mzk.recordmanager.server.imports;

import com.ctc.wstx.exc.WstxEOFException;
import com.ctc.wstx.exc.WstxParsingException;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.XMLEventReaderUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ManuscriptoriumFulltextXmlStreamReader {

	private XMLInputFactory xmlFactory;

	private XMLEventReader eventReader;

	private static final String ELEMENT_UNCIPIT = "incipit";
	private static final String ELEMENT_EXPLICIT = "explicit";
	private static final String ELEMENT_COLOFON = "colofon";
	private static final String ELEMENT_QUOTE = "quote";
	private static final String ELEMENT_TITLE = "title";
	private static final String ELEMENT_RUBRIC = "rubric";

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public ManuscriptoriumFulltextXmlStreamReader(InputStream input) {
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
		StringBuilder result = new StringBuilder();
		List<String> titles = new ArrayList<>();
		while (eventReader.hasNext()) {
			try {
				XMLEvent xmlEvent = eventReader.nextEvent();

				if (xmlEvent.isStartElement()) {
					switch (xmlEvent.asStartElement().getName().getLocalPart()) {
					case ELEMENT_UNCIPIT:
					case ELEMENT_EXPLICIT:
					case ELEMENT_COLOFON:
					case ELEMENT_QUOTE:
					case ELEMENT_TITLE:
					case ELEMENT_RUBRIC:
						String xmlFragment = readElementBody(eventReader);
						if (!titles.contains(xmlFragment)) {
							result.append(xmlFragment).append("\n");
							titles.add(xmlFragment);
						}

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

	private static final Pattern REPLACE = Pattern.compile("<[^>]*>");

	private static String readElementBody(final XMLEventReader eventReader) throws XMLStreamException {
		String result = XMLEventReaderUtils.readElementBody(eventReader);
		return CleaningUtils.replaceAll(result, REPLACE, "");
	}
}
