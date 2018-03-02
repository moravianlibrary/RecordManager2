package cz.mzk.recordmanager.server;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceProvider {

	InputStream getResource(String resourcePath) throws IOException;

}
