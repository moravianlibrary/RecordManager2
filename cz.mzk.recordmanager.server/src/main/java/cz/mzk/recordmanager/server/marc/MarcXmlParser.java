package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;

import org.marc4j.marc.Record;

public interface MarcXmlParser {

	public MarcRecord parseRecord(InputStream is);
	
	public Record parseUnderlyingRecord(InputStream is);

}
