package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;

public interface MarcXmlParser {

	public MarcRecord parseRecord(InputStream is);

}
