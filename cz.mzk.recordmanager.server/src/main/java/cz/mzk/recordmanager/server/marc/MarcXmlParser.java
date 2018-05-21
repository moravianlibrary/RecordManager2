package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.marc4j.marc.Record;

public interface MarcXmlParser {

	MarcRecord parseRecord(InputStream is);

	MarcRecord parseRecord(byte[] rawRecord);

	MarcRecord parseRecord(HarvestedRecord hr);
	
	Record parseUnderlyingRecord(InputStream is);

	Record parseUnderlyingRecord(byte[] rawRecord);

	Record parseUnderlyingRecord(HarvestedRecord hr);

}
