package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;

import org.marc4j.RecordStack;
import org.marc4j.marc.Record;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import cz.mzk.recordmanager.server.marc.marc4j.MarcXmlHandler;

@Component
public class MarcXmlParserImpl implements MarcXmlParser {

	@Override
	public MarcRecord parseRecord(InputStream is) {
		try {
			RecordStack queue = new RecordStack();
			MarcXmlHandler handler = new MarcXmlHandler(queue);
			org.marc4j.MarcXmlParser parser = new org.marc4j.MarcXmlParser(
					handler);
			parser.parse(new InputSource(is));
			Record record = queue.pop();
			return new MarcRecordImpl(record);
		} catch (org.marc4j.MarcException me) {
			throw new InvalidMarcException(me.getMessage(), me);
		}
	}

}
