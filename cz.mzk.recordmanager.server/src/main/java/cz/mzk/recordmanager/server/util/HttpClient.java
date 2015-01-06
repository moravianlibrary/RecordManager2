package cz.mzk.recordmanager.server.util;

import java.io.IOException;
import java.io.InputStream;

public interface HttpClient {
	
	public InputStream executeGet(String url) throws IOException;

}
