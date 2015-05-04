package cz.mzk.recordmanager.server.dc;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.stereotype.Component;

@Component
public class DublinCoreParserXMLImpl implements DublinCoreParser {

	@Override
	public DublinCoreRecord parseRecord(InputStream is) {
		try {
			DublinCoreXMLHandler handler = new DublinCoreXMLHandler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(is, handler);

			return handler.getRecord();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
