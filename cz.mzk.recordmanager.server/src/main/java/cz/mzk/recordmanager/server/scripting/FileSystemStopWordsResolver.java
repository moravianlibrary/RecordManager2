package cz.mzk.recordmanager.server.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemStopWordsResolver implements StopWordsResolver {

	private static Logger logger = LoggerFactory.getLogger(FileSystemStopWordsResolver.class);

	protected final File baseDirectory;

	public FileSystemStopWordsResolver(File baseDirectory) {
		super();
		this.baseDirectory = baseDirectory;
	}

	@Override
	public Set<String> resolve(String filename) throws IOException {
		File file = new File(baseDirectory, filename);
		if (!file.canRead()) {
			throw new IllegalArgumentException(String.format("File %s cannot be read", file.getAbsolutePath()));
		}
		logger.trace("Loading stop words from file {}", file.getAbsolutePath());
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.toCollection(HashSet::new));
		}
	}

}
