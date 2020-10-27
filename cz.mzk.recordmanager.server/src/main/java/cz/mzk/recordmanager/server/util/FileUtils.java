package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FileUtils {

	public static List<String> openFile(final String resourcePath) {
		return new BufferedReader(new InputStreamReader(
				new ClasspathResourceProvider().getResource(resourcePath), StandardCharsets.UTF_8))
				.lines().collect(Collectors.toCollection(ArrayList::new));
	}

	public static List<String> getFilesName(String filename) throws FileNotFoundException {
		List<String> files = new ArrayList<>();
		File f = new File(filename);
		if (f.isFile()) {
			files.add(f.getAbsolutePath());
		} else {
			File[] listFiles = f.listFiles();
			if (listFiles == null) throw new FileNotFoundException();
			for (File file : listFiles) {
				if (file.isDirectory()) getFilesName(file.getAbsolutePath());
				else files.add(file.getAbsolutePath());
			}
		}
		return files;
	}
}
