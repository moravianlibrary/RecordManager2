package cz.mzk.recordmanager.server.util;

import java.text.Normalizer;

import org.apache.commons.validator.routines.ISBNValidator;

public class MetadataUtils {

	private static final ISBNValidator isbnValidator = ISBNValidator.getInstance(true);

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

	public static String normalizeAndShorten(final String input, int length) {
		String normalized = normalize(input);
		if (normalized == null) {
			return null;
		}
		return normalized.substring(0, Math.min(length, normalized.length()));
	}

	public static Long toISBN13(String isbn) {
		String isbn13 = isbnValidator.validate(isbn);
		return (isbn13 == null)? null: Long.valueOf(isbn13);
	}

}
