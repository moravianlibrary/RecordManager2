package cz.mzk.recordmanager.server.marc.intercepting;

import java.nio.charset.StandardCharsets;

import org.marc4j.marc.Record;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;

public class DefaultMarcInterceptor implements MarcRecordInterceptor {
	
	private Record record;
	
	public DefaultMarcInterceptor(Record record) {
		this.record = record;
	}
	
	@Override
	public byte[] intercept() {
		MarcRecord marcRecord = new MarcRecordImpl(record);
		return marcRecord.export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}
	
	protected Record getRecord() {
		return this.record;
	}
}
