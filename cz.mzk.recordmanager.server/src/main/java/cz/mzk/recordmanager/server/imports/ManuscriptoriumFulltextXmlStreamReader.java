package cz.mzk.recordmanager.server.imports;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.ctc.wstx.exc.WstxEOFException;
import com.ctc.wstx.exc.WstxParsingException;

public class ManuscriptoriumFulltextXmlStreamReader {

	private XMLInputFactory xmlFactory;

	private XMLEventReader eventReader;

	private static final String ELEMENT_UNCIPIT = "incipit";
	private static final String ELEMENT_EXPLICIT = "explicit";
	private static final String ELEMENT_COLOFON = "colofon";
	private static final String ELEMENT_QUOTE = "quote";
	private static final String ELEMENT_TITLE = "title";

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
		String result = "";
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
						String xmlFragment = readElementBody(eventReader);
						if (!titles.contains(xmlFragment)) {
							result += xmlFragment + "\n";
							titles.add(xmlFragment);
						}

						break;
					default:
						break;
					}
				}
			} catch (WstxEOFException e) {
				break;
			} catch (WstxParsingException e) {
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	protected String readElementBody(XMLEventReader eventReader)
			throws XMLStreamException {
		StringWriter buf = new StringWriter();

		int depth = 0;
		while (eventReader.hasNext()) {
			// peek event
			XMLEvent xmlEvent = eventReader.peek();

			if (xmlEvent.isStartElement())
				++depth;
			else if (xmlEvent.isEndElement()) {
				--depth;
				// reached END_ELEMENT tag?
				// break loop, leave event in stream
				if (depth < 0)
					break;
			}

			xmlEvent = eventReader.nextEvent();
			xmlEvent.writeAsEncodedUnicode(buf);
		}

		return buf.getBuffer().toString();
	}
}
