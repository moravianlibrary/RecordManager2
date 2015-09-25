package cz.mzk.recordmanager.server.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClasspathStopWordsResolver implements StopWordsResolver {

	private final String root = "stopwords/";

	@Override
	public Set<String> resolve(String file) throws IOException {
		String path = root + file;
		InputStream is = getClass().getClassLoader().getResourceAsStream(path);
		if (is == null) {
			throw new IllegalArgumentException(
					String.format("File '%s' with stopwords not found", path));
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.toCollection(HashSet::new));
		}
	}

}
