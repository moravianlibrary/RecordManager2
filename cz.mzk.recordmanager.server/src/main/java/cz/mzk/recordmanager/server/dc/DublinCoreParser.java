package cz.mzk.recordmanager.server.dc;

	import java.io.InputStream;

	public interface DublinCoreParser {

		public DublinCoreRecord parseRecord(InputStream is);

	}
