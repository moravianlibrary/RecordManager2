package cz.mzk.recordmanager.server.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemMappingResolver implements MappingResolver {

	private static Logger logger = LoggerFactory.getLogger(FileSystemMappingResolver.class);

	protected final File baseDirectory;

	public FileSystemMappingResolver(File baseDirectory) {
		super();
		this.baseDirectory = baseDirectory;
	}

	@Override
	public Mapping resolve(String filename) throws IOException {
		File file = new File(baseDirectory, filename);
		if (!file.canRead()) {
			throw new IllegalArgumentException(String.format("File %s cannot be read", file.getAbsolutePath()));
		}
		logger.trace("Loading mapping from file {}", file.getAbsolutePath());
		try (InputStream is = new FileInputStream(file)) {
			return new Mapping(new FileInputStream(file));
		}
	}

}
