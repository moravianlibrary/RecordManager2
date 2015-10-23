package cz.mzk.recordmanager.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class FileSystemResourceProvider implements ResourceProvider {

	private static Logger logger = LoggerFactory.getLogger(FileSystemResourceProvider.class);

	protected final File baseDirectory;

	public FileSystemResourceProvider(File baseDirectory) {
		super();
		Preconditions.checkNotNull(baseDirectory, "baseDirectory");
		this.baseDirectory = baseDirectory;
	}

	@Override
	public InputStream getResource(String resourcePath) throws IOException {
		File file = new File(baseDirectory, resourcePath);
		if (file.canRead()) {
			logger.trace("Opening resource {} from file system", file.getAbsolutePath());
			return new FileInputStream(file);
		}
		return null;
	}

}
