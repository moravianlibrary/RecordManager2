package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.model.Ean;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EANUtils {

	private static final Pattern EAN_PATTERN = Pattern.compile("[0-9]{13}");
	private static final Pattern EAN_NOTE_PATTERN = Pattern.compile("([0-9]*)(.*)");

	private EANUtils() {
	}

	/**
	 * @param rawDataField {@link DataField}, usually tag 024, 1. indicator = 3
	 * @return {@link Ean}
	 */
	public static Ean createEan(final DataField rawDataField) {
		Subfield subfieldA = rawDataField.getSubfield('a');
		if (rawDataField.getIndicator1() != '3' || subfieldA == null)
			throw new NoDataException(IdentifierUtils.NO_SUBFIELD);

		Matcher matcher = EAN_NOTE_PATTERN.matcher(subfieldA.getData());
		if (!matcher.find()) throw new NoDataException(IdentifierUtils.NO_USABLE_DATA);

		Long validEan = getEAN13Long(matcher.group(1));
		if (validEan == null) throw new NumberFormatException(matcher.group(1));

		List<String> notes = new ArrayList<>();
		notes.add(IdentifierUtils.parseNote(matcher.group(2)));

		for (Subfield subfieldQ : rawDataField.getSubfields('q')) {
			notes.add(IdentifierUtils.parseNote(subfieldQ.getData()));
		}
		return Ean.create(validEan, 1L, String.join(" ", notes));
	}

	public static boolean isEAN13valid(final String ean) {
		// add 0 when length is 12, 13 is OK
		String localEan = (ean.length() == 12) ? '0' + ean : ean;
		if (EAN_PATTERN.matcher(localEan).matches()) {
			int sumOdd = 0;
			int sumEven = 0;
			for (int i = 1; i < 13; i++) {
				if (i % 2 == 0) sumEven += Character.getNumericValue(localEan.charAt(i));
				else sumOdd += Character.getNumericValue(localEan.charAt(i));
			}

			sumOdd *= 3;
			int sum = sumEven + sumOdd + Character.getNumericValue(localEan.charAt(0));
			if (sum % 10 == 0) return true;
		}

		return false;
	}

	public static Long getEAN13Long(final String rawEan) {
		return isEAN13valid(rawEan) ? Long.valueOf(rawEan) : null;
	}

}
