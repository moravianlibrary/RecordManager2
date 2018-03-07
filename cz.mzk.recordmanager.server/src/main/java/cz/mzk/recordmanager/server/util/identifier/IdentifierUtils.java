package cz.mzk.recordmanager.server.util.identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IdentifierUtils {

	static final String NO_SUBFIELD = "No subfield 'a'";
	static final String NO_USABLE_DATA = "No usable data";
	private static final Pattern NOTE_PATTERN = Pattern.compile("\\s*\\((.+)\\)\\s*");

	private IdentifierUtils() {
	}

	/**
	 * @param rawNote String represention of note
	 * @return String
	 */
	static String parseNote(final String rawNote) {
		Matcher matcherNote;
		return (matcherNote = NOTE_PATTERN.matcher(rawNote)).matches() ? matcherNote.group(1) : rawNote;
	}
}
