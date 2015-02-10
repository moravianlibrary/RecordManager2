package cz.mzk.recordmanager.server.util;

import java.text.Normalizer;

public class MetadataUtils {

	public static boolean hasTrailingPunctuation(final String input) {
		return input.matches(".*(([:;,=\\(\\[])|(\\s\\.))$");
	}

	public static String normalize(final String input) {
		return input == null ? null : Normalizer
				.normalize(input, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.replaceAll("\\W", "")
				.toLowerCase();
	}
}
