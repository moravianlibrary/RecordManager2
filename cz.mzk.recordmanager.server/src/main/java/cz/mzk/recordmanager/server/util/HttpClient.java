package cz.mzk.recordmanager.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface HttpClient {

	InputStream executeGet(String url) throws IOException;

	InputStream executeGet(String url, Map<String, String> headers) throws IOException;

}
