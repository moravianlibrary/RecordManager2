package cz.mzk.recordmanager.server;

import java.io.InputStream;

public class ClasspathResourceProvider implements ResourceProvider {

	@Override
	public InputStream getResource(String resourcePath) {
		return getClass().getResourceAsStream(resourcePath);
	}

}
