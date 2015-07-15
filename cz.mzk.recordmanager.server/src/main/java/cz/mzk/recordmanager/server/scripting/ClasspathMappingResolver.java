package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * Mapping resolver that reads translation files from classpath from mapping package.
 * 
 * @author xrosecky
 *
 */
public class ClasspathMappingResolver implements MappingResolver {

	private final String root = "mapping/";

	@Override
	public Mapping resolve(String file) throws IOException {
		String path = root + file;
		InputStream is = getClass().getClassLoader().getResourceAsStream(path);
		if (is == null) {
			throw new IllegalArgumentException(
					String.format("Mapping '%s' not found", path));
		}
		Mapping mapping = new Mapping(is);
		return mapping;
	}

}
