package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;

import java.io.BufferedReader;
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
}
