package cz.mzk.recordmanager.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CleaningUtils {

	public static List<String> replaceFirst(List<String> input, Pattern pattern, String replacement) {
		return input.stream().map(it -> replace(it, pattern, replacement, false)).collect(Collectors.toCollection(ArrayList::new));
	}

	public static List<String> replaceAll(List<String> input, Pattern pattern, String replacement) {
		return input.stream().map(it -> replace(it, pattern, replacement, true)).collect(Collectors.toCollection(ArrayList::new));
	}

	public static String replaceAll(String input, Pattern pattern, String replacement) {
		return replace(input, pattern, replacement, true);
	}

	public static String replaceFirst(String input, Pattern pattern, String replacement) {
		return replace(input, pattern, replacement, false);
	}

	public static List<String> extract(List<String> input, Pattern pattern) {
		return input.stream().map(it -> extract(it, pattern)).collect(Collectors.toCollection(ArrayList::new));
	}

	public static String extract(String input, Pattern pattern) {
		Matcher matcher = pattern.matcher(input);
		return (matcher.find()) ? matcher.group(1) : input; 
	}

	protected static String replace(String input, Pattern pattern, String replacement, boolean all) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			return (all) ? matcher.replaceAll(replacement) : matcher.replaceFirst(replacement);
		}
		return input;
	}

}
