package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.model.Isbn;
import org.apache.commons.validator.routines.ISBNValidator;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ISBNUtils {

	private static final ISBNValidator VALIDATOR = ISBNValidator.getInstance(true);
	private static final Pattern ISBN_PATTERN = Pattern.compile("([\\dxX\\s\\-]*)(.*)");
	private static final Pattern NOTE_PATTERN = Pattern.compile("\\((.+)\\)");
	private static final Pattern ISBN_CLEAR_REGEX = Pattern.compile("[^0-9Xx]");
	private static final Pattern ISBN_UPPER_X_REGEX = Pattern.compile("x");

	private static final String NO_SUBFIELD = "No subfield 'a'";
	private static final String NO_USABLE_DATA = "No usable data in subfield 'a'";

	private ISBNUtils() {
	}

	/**
	 * @param rawDataField {@link DataField}, usually tag 020
	 * @return {@link Isbn}
	 */
	public static Isbn createIsbn(final DataField rawDataField) {
		Subfield subfieldA = rawDataField.getSubfield('a');
		if (subfieldA == null) throw new NoDataException(NO_SUBFIELD);

		Matcher matcherIsbn = ISBN_PATTERN.matcher(subfieldA.getData());
		if (!matcherIsbn.find()) throw new NoDataException(NO_USABLE_DATA);

		Isbn isbn = new Isbn();
		isbn.setIsbn(toISBN13Long(matcherIsbn.group(1)));

		List<String> notes = new ArrayList<>();
		notes.add(parseNote(matcherIsbn.group(2)));

		for (Subfield subfieldQ : rawDataField.getSubfields('q')) {
			notes.add(parseNote(subfieldQ.getData()));
		}
		isbn.setNote(String.join(" ", notes));
		isbn.setOrderInRecord(1L);
		return isbn;
	}

	/**
	 * @param rawIsbn String to be validated
	 * @return String representation of ISBN 13
	 */
	static String toISBN13String(final String rawIsbn) {
		String clearIsbnStr = ISBN_CLEAR_REGEX.matcher(rawIsbn).replaceAll("");
		clearIsbnStr = ISBN_UPPER_X_REGEX.matcher(clearIsbnStr).replaceAll("X");
		String validatedIsbn = VALIDATOR.validate(clearIsbnStr);
		if (validatedIsbn == null) throw new NumberFormatException(rawIsbn);
		return validatedIsbn;
	}

	/**
	 * @param rawIsbn String to be validated
	 * @return Long representation of ISBN 13
	 */
	static Long toISBN13Long(final String rawIsbn) {
		return Long.valueOf(toISBN13String(rawIsbn));
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
