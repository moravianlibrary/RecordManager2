package cz.mzk.recordmanager.server.dc;

import java.io.InputStream;

public interface DublinCoreParser {

	DublinCoreRecord parseRecord(InputStream is);

}
