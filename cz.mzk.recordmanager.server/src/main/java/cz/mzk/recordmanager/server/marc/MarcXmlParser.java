package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;

import org.marc4j.marc.Record;

public interface MarcXmlParser {

	MarcRecord parseRecord(InputStream is);

	MarcRecord parseRecord(byte[] rawRecord);
	
	Record parseUnderlyingRecord(InputStream is);

	Record parseUnderlyingRecord(byte[] rawRecord);

}
