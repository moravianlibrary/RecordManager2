package cz.mzk.recordmanager.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class RomanNumeralsUtils {

	private static final Map<Pattern, String> ROMAN_NUMERALS = new HashMap<>();

	static {
		ROMAN_NUMERALS.put(Pattern.compile("\\b1\\b"), "I");
		ROMAN_NUMERALS.put(Pattern.compile("\\b2\\b"), "II");
		ROMAN_NUMERALS.put(Pattern.compile("\\b3\\b"), "III");
		ROMAN_NUMERALS.put(Pattern.compile("\\b4\\b"), "IV");
		ROMAN_NUMERALS.put(Pattern.compile("\\b5\\b"), "V");
		ROMAN_NUMERALS.put(Pattern.compile("\\b6\\b"), "VI");
		ROMAN_NUMERALS.put(Pattern.compile("\\b7\\b"), "VII");
		ROMAN_NUMERALS.put(Pattern.compile("\\b8\\b"), "VIII");
		ROMAN_NUMERALS.put(Pattern.compile("\\b9\\b"), "IX");
	}

	public static String getRomanNumerals(final String text) {
		String result = text;
		for (Pattern pattern : ROMAN_NUMERALS.keySet()) {
			result = CleaningUtils.replaceAll(result, pattern, ROMAN_NUMERALS.get(pattern));
		}
		return result;
	}
}
