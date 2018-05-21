package cz.mzk.recordmanager.server.marc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
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
			org.marc4j.MarcXmlParser parser = new org.marc4j.MarcXmlParser(handler);
			parser.parse(new InputSource(is));
			Record record = queue.pop();
			return new MarcRecordImpl(record);
		} catch (org.marc4j.MarcException me) {
			throw new InvalidMarcException(me.getMessage(), me);
		}
	}

	@Override
	public MarcRecord parseRecord(byte[] rawRecord) {
		return parseRecord(new ByteArrayInputStream(rawRecord));
	}

	@Override
	public MarcRecord parseRecord(HarvestedRecord hr) {
		return parseRecord(hr.getRawRecord());
	}

	@Override
	public Record parseUnderlyingRecord(InputStream is) {
		try {
			RecordStack queue = new RecordStack();
			MarcXmlHandler handler = new MarcXmlHandler(queue);
			org.marc4j.MarcXmlParser parser = new org.marc4j.MarcXmlParser(handler);
			parser.parse(new InputSource(is));
			return queue.pop();
		} catch (org.marc4j.MarcException me) {
			throw new InvalidMarcException(me.getMessage(), me);
		}
	}

	@Override
	public Record parseUnderlyingRecord(byte[] rawRecord) {
		return parseUnderlyingRecord(new ByteArrayInputStream(rawRecord));
	}

	@Override
	public Record parseUnderlyingRecord(HarvestedRecord hr) {
		return parseUnderlyingRecord(hr.getRawRecord());
	}

}
