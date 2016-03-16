package cz.mzk.recordmanager.server.dc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXParseException;

@Component
public class DublinCoreParserXMLImpl implements DublinCoreParser {

	@Override
	public DublinCoreRecord parseRecord(InputStream is) {
		try {
			
			byte[] input = IOUtils.toByteArray(is);
			DublinCoreXMLHandler handler = new DublinCoreXMLHandler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new ByteArrayInputStream(input), handler);
			
			DublinCoreRecord dcRecord = handler.getRecord();
			dcRecord.setRawRecord(input);
			return handler.getRecord();
		} catch (SAXParseException se) {
			throw new InvalidDcException (se.getMessage(), se);
		} catch (InvalidDcException dce) {
			//InvalidDcException is thrown by handler, when record is empty (not DC etc.)
			throw dce;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
