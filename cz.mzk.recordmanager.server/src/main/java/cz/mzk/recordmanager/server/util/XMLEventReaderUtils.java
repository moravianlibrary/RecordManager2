package cz.mzk.recordmanager.server.util;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.StringWriter;

public class XMLEventReaderUtils {

	public static String readElementBody(final XMLEventReader eventReader) throws XMLStreamException {
		StringWriter buf = new StringWriter();
		int depth = 0;

		while (eventReader.hasNext()) {
			// peek event
			XMLEvent xmlEvent = eventReader.peek();
			if (xmlEvent.isStartElement()) ++depth;
			else if (xmlEvent.isEndElement()) {
				--depth;
				// reached END_ELEMENT tag?
				// break loop, leave event in stream
				if (depth < 0) break;
			}
			xmlEvent = eventReader.nextEvent();
			xmlEvent.writeAsEncodedUnicode(buf);
		}
		return buf.getBuffer().toString();
	}

}
