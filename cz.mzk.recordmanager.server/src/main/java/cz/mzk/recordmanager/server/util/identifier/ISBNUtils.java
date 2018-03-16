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
	private static final Pattern ISBN_CLEAR_REGEX = Pattern.compile("[^0-9Xx]");
	private static final Pattern ISBN_UPPER_X_REGEX = Pattern.compile("x");

	private ISBNUtils() {
	}

	/**
	 * @param rawDataField {@link DataField}, usually tag 020
	 * @return {@link Isbn}
	 */
	public static Isbn createIsbn(final DataField rawDataField) {
		Subfield subfieldA = rawDataField.getSubfield('a');
		if (subfieldA == null) throw new NoDataException(IdentifierUtils.NO_SUBFIELD);

		Matcher matcherIsbn = ISBN_PATTERN.matcher(subfieldA.getData());
		if (!matcherIsbn.find()) throw new NoDataException(IdentifierUtils.NO_USABLE_DATA);

		Long isbnLong = toISBN13LongThrowing(matcherIsbn.group(1));

		List<String> notes = new ArrayList<>();
		notes.add(IdentifierUtils.parseNote(matcherIsbn.group(2)));

		for (Subfield subfieldQ : rawDataField.getSubfields('q')) {
			notes.add(IdentifierUtils.parseNote(subfieldQ.getData()));
		}

		return Isbn.create(isbnLong, 1L, String.join(" ", notes));
	}

	/**
	 * @param rawIsbn String to be validated
	 * @return String representation of ISBN 13 or exception
	 */
	static String toISBN13StringThrowing(final String rawIsbn) {
		if (rawIsbn == null || rawIsbn.trim().isEmpty()) throw new NoDataException(IdentifierUtils.NO_USABLE_DATA);
		String clearIsbnStr = ISBN_CLEAR_REGEX.matcher(rawIsbn).replaceAll("");
		clearIsbnStr = ISBN_UPPER_X_REGEX.matcher(clearIsbnStr).replaceAll("X");
		String validatedIsbn = VALIDATOR.validate(clearIsbnStr);
		if (validatedIsbn == null) throw new NumberFormatException(rawIsbn);
		return validatedIsbn;
	}

	/**
	 * @param rawIsbn String to be validated
	 * @return String representation of ISBN 13 or null
	 */
	public static String toISBN13String(final String rawIsbn) {
		try {
			return toISBN13StringThrowing(rawIsbn);
		} catch (NumberFormatException | NoDataException nfe) {
			return null;
		}
	}

	/**
	 * @param rawIsbn String to be validated
	 * @return Long representation of ISBN 13 or exception
	 */
	public static Long toISBN13LongThrowing(final String rawIsbn) {
		return Long.valueOf(toISBN13StringThrowing(rawIsbn));
	}

	/**
	 * @param rawIsbn String to be validated
	 * @return Long representation of ISBN 13 or null
	 */
	public static Long toISBN13Long(final String rawIsbn) {
		try {
			return Long.valueOf(toISBN13StringThrowing(rawIsbn));
		} catch (NumberFormatException | NoDataException nfe) {
			return null;
		}
	}

}
