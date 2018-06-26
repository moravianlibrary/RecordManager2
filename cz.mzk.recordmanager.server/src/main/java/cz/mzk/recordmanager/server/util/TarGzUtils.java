package cz.mzk.recordmanager.server.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

public final class TarGzUtils {

	private static final Logger logger = LoggerFactory.getLogger(TarGzUtils.class);

	public static void extract(File tarFile, File destFile) throws IOException {
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

	public static File compress(String sourcePath, String targetPath) throws IOException {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;
		File targetFile = new File(targetPath);
		try {
			fOut = new FileOutputStream(targetFile);
			bOut = new BufferedOutputStream(fOut);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);
			tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
			addFileToTarGz(tOut, sourcePath, "");
		} finally {
			if (tOut != null) {
				tOut.finish();
				tOut.close();
			}
			if (gzOut != null) gzOut.close();
			if (bOut != null) bOut.close();
			if (fOut != null) fOut.close();
		}
		return targetFile;
	}

	private static void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base) throws IOException {
		File file = new File(path);
		String entryName = base + file.getName();
		TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
		tOut.putArchiveEntry(tarEntry);

		if (file.isFile()) {
			FileInputStream in = new FileInputStream(file);
			IOUtils.copy(in, tOut);
			in.close();
			tOut.closeArchiveEntry();
		} else {
			tOut.closeArchiveEntry();
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToTarGz(tOut, child.getAbsolutePath(), entryName + '/');
				}
			}
		}
	}

}
