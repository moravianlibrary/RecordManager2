package cz.mzk.recordmanager.server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpExtractor {

	private final Pattern extractor;

	private final String replacement;

	public RegexpExtractor(String regex) {
		if (!regex.startsWith("s/")) {
			this.extractor = Pattern.compile(regex);
			this.replacement = null;
		} else {
			String[] parts = regex.split("/");
			this.extractor = Pattern.compile(parts[1]);
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
