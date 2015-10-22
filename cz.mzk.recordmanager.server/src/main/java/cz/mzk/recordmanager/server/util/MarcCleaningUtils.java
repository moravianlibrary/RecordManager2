package cz.mzk.recordmanager.server.util;

import java.util.List;
import java.util.regex.Pattern;

public class MarcCleaningUtils {

	private static final String EMPTY_STRING = "";

	private static final Pattern CLEAN_FACET_REGEX = Pattern.compile("(?<!\\b[A-Z])[.\\s]*$");

	public static List<String> cleanFacets(List<String> facets) {
		return CleaningUtils.replaceFirst(facets, CLEAN_FACET_REGEX, EMPTY_STRING);
	}

}
