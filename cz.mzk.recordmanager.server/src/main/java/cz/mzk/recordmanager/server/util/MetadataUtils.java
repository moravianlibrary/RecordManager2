package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.ShortTitle;
import cz.mzk.recordmanager.server.model.Title;
import org.apache.commons.lang3.tuple.Pair;
import org.marc4j.marc.DataField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MetadataUtils {

	private static final List<String> SIMILARITY_WORDS = new BufferedReader(new InputStreamReader(
			new ClasspathResourceProvider().getResource("/mapping/similarity_words.map"), StandardCharsets.UTF_8)) 
			.lines().collect(Collectors.toCollection(ArrayList::new));

	private static final Pattern TRAILINGPUNCTUATION_PATTERN = Pattern.compile(".*(([:;,=(\\[])|(\\s\\.))$");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d");
	private static final List<Pattern> PATTERNS = SIMILARITY_WORDS.stream()
			.map(w -> Pattern.compile("\\b" + w + "\\b", Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());
	private static final Pattern NORMALIZE_DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	private static final Pattern NORMALIZE_NON_WORD_PATTERN = Pattern.compile("\\W");

	private static final Pattern FIELD_245 = Pattern.compile("245");

	private static final List<Pair<Pattern, String>> REPLACEMENT_PAIRS = new ArrayList<>();

	static {
		REPLACEMENT_PAIRS.add(Pair.of(Pattern.compile("ß"), "ss"));
	}

	public static boolean hasTrailingPunctuation(final String input) {
		return TRAILINGPUNCTUATION_PATTERN.matcher(input).matches();
	}

	public static String normalize(final String input) {
		if (input == null) return null;
		String result = Normalizer.normalize(input, Normalizer.Form.NFD);
		for (Pair<Pattern, String> pair : REPLACEMENT_PAIRS) {
			result = CleaningUtils.replaceAll(result, pair.getLeft(), pair.getRight());
		}
		result = CleaningUtils.replaceAll(result, NORMALIZE_DIACRITICS_PATTERN, "");
		result = CleaningUtils.replaceAll(result, NORMALIZE_NON_WORD_PATTERN, "");
		return result.toLowerCase();
	}

	public static String normalizeAndShorten(final String input, int length) {
		String normalized = normalize(input);
		if (normalized == null) {
			return null;
		}
		return normalized.substring(0, Math.min(length, normalized.length()));
	}

	public static String shorten(String input, int length) {
		if (input == null) return null;
		return input.substring(0, Math.min(length, input.length()));
	}

	public static boolean similarityEnabled(DataField df, Title title) {
		return similarityEnabled(df, title.getTitleStr());
	}

	public static boolean similarityEnabled(DataField df, ShortTitle shortTitle) {
		return similarityEnabled(df, shortTitle.getShortTitleStr());
	}

	public static boolean similarityEnabled(DataField df, String title) {
		if (FIELD_245.matcher(df.getTag()).matches() && df.getSubfield('n') != null) {
			return false;
		}
		return similarityEnabled(title);
	}

	public static boolean similarityEnabled(Title title){
		return similarityEnabled(title.getTitleStr());
	}
	
	public static boolean similarityEnabled(ShortTitle shortTitle) {
		return similarityEnabled(shortTitle.getShortTitleStr());
	}
	
	public static boolean similarityEnabled(String title) {
		if (NUMBER_PATTERN.matcher(title).find()) return false;
		for (Pattern pattern: PATTERNS) {
			if (pattern.matcher(title).find()) {
				return false;
			}
		}
		return true;
	}

	public static boolean containsChar(char[] array, char charToFind) {
		for (char c : array) {
			if (c == charToFind) return true;
		}
		return false;
	}

	public static String generateUrl(final String source, final String availability, final String link
			, final String comment) {
		return String.format("%s|%s|%s|%s", source, availability, link, comment);
	}

	public static String getPrefix(ImportConfiguration configuration) {
		if (configuration != null) {
			String prefix;
			prefix = configuration.getIdPrefix();
			return prefix == null ? "" : prefix;
		}
		return "";
	}

	public static String getPrefix(HarvestedRecord hr) {
		return getPrefix(hr.getHarvestedFrom());
	}

}
