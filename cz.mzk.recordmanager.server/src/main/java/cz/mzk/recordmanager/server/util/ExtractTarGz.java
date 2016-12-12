package cz.mzk.recordmanager.server.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractTarGz {

	private static Logger logger = LoggerFactory.getLogger(ExtractTarGz.class);
	
	public static void extractTarGz(File tarFile, File destFile) throws IOException {
		logger.info("Extracting file: " + tarFile.getName());
		destFile.mkdir();
		TarArchiveInputStream tarIn = null;

		tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(
				new BufferedInputStream(new FileInputStream(tarFile))));

		TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
		while (tarEntry != null) {// create a file with the same name as the
									// tarEntry
			File destPath = new File(destFile, tarEntry.getName());
			if (tarEntry.isDirectory()) {
				destPath.mkdirs();
			} else {
				destPath.createNewFile();
				// byte [] btoRead = new byte[(int)tarEntry.getSize()];
				byte[] btoRead = new byte[1024];
				// FileInputStream fin
				// = new FileInputStream(destPath.getCanonicalPath());
				BufferedOutputStream bout = new BufferedOutputStream(
						new FileOutputStream(destPath));
				int len = 0;

				while ((len = tarIn.read(btoRead)) != -1) {
					bout.write(btoRead, 0, len);
				}
				bout.close();
				btoRead = null;
			}
			tarEntry = tarIn.getNextTarEntry();
		}
		tarIn.close();
	}
}
