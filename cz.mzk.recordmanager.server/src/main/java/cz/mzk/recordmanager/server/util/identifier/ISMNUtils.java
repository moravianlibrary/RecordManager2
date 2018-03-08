package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.model.Ismn;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ISMNUtils {

	private static final Pattern ISMN_PATTERN = Pattern.compile("([\\dM\\s\\-]*)(.*)");
	private static final Pattern ISMN_CLEAR_REGEX = Pattern.compile("[^0-9M]");
	private static final Pattern ISMN10_PREFIX = Pattern.compile("^M");
	private static final String ISMN13_PREFIX = "9790";

	private ISMNUtils() {
	}

	/**
	 * @param rawDataField {@link DataField}, usually tag 024, 1. indicator = 2
	 * @return {@link Ismn}
	 */
	public static Ismn createIsmn(final DataField rawDataField) {
		Subfield subfieldA = rawDataField.getSubfield('a');
		if (rawDataField.getIndicator1() != '2' || subfieldA == null)
			throw new NoDataException(IdentifierUtils.NO_SUBFIELD);

		Matcher matcher = ISMN_PATTERN.matcher(subfieldA.getData());
		if (!matcher.find()) throw new NoDataException(IdentifierUtils.NO_USABLE_DATA);

		Long validIsmn = toIsmn13LongThrowing(matcher.group(1));

		List<String> notes = new ArrayList<>();
		notes.add(IdentifierUtils.parseNote(matcher.group(2)));

		for (Subfield subfieldQ : rawDataField.getSubfields('q')) {
			notes.add(IdentifierUtils.parseNote(subfieldQ.getData()));
		}
		return createIsmn(validIsmn, 1L, String.join(" ", notes));
	}

	public static Ismn createIsmn(final Long ismn, final long orderInRecord, final String note) {
		Ismn newIsmn = new Ismn();
		newIsmn.setIsmn(ismn);
		newIsmn.setOrderInRecord(orderInRecord);
		newIsmn.setNote(note);
		return newIsmn;
	}

	/**
	 * @param ismn String to be validated
	 * @return String representation of ISMN 13 or exception
	 */
	public static Long toIsmn13LongThrowing(final String ismn) {
		String resultIsmn = ISMN_CLEAR_REGEX.matcher(ismn).replaceAll("");
		resultIsmn = ISMN10_PREFIX.matcher(resultIsmn).replaceAll(ISMN13_PREFIX);
		if (resultIsmn.length() != 13) throw new NumberFormatException(ismn);
		return Long.parseLong(resultIsmn);
	}


}
