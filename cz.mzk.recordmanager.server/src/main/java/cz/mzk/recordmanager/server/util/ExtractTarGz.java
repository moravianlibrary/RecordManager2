package cz.mzk.recordmanager.server.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

public final class ExtractTarGz {

	private static final Logger logger = LoggerFactory.getLogger(ExtractTarGz.class);

	public static void extractTarGz(File tarFile, File destFile) throws IOException {
		logger.info("Extracting file: " + tarFile.getName());
		if (destFile.exists()) throw new FileAlreadyExistsException(destFile.getAbsolutePath());
		if (!destFile.mkdir()) throw new IOException("Can't make directory for " + destFile.getAbsolutePath());

		TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(
				new BufferedInputStream(new FileInputStream(tarFile))));
		TarArchiveEntry tarEntry = tarIn.getNextTarEntry();

		while (tarEntry != null) {
			// create a file with the same name as the tarEntry
			File destPath = new File(destFile, tarEntry.getName());
			if (tarEntry.isDirectory()) {
				if (!destPath.mkdirs()) logger.info("Can't make directory for " + destPath.getAbsolutePath());
			} else {
				if (!destPath.createNewFile()) logger.info("Name of file already exists " + destPath.getAbsolutePath());
				BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath));
				byte[] btoRead = new byte[1024];
				int len;
				while ((len = tarIn.read(btoRead)) != -1) {
					bout.write(btoRead, 0, len);
				}
				bout.close();
			}
			tarEntry = tarIn.getNextTarEntry();
		}
		tarIn.close();
	}
}
