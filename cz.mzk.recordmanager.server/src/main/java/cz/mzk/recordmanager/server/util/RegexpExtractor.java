package cz.mzk.recordmanager.server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpExtractor {

	private static final Pattern SPLIT = Pattern.compile("(?<=[^\\\\])/");
	private static final Pattern REPLACE_PATTERN = Pattern.compile("\\\\/", Pattern.LITERAL);

	private final Pattern extractor;

	private final String replacement;

	public RegexpExtractor(String regex) {
		if (!regex.startsWith("s/")) {
			this.extractor = Pattern.compile(regex);
			this.replacement = null;
		} else {
			// \\/ is not separator
			String[] parts = SPLIT.split(regex);
			this.extractor = Pattern.compile(CleaningUtils.replaceAll(parts[1], REPLACE_PATTERN, "/"));
			this.replacement = parts[2];
		}
	}

	public String extract(String value) {
		Matcher matcher = extractor.matcher(value);
		if (!matcher.matches()) {
			return value;
		}
		return (replacement == null) ? matcher.group(1) : matcher.replaceAll(replacement);
	}

}
