package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.model.Issn;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ISSNUtils {

	private static final Pattern ISSN_PATTERN = Pattern.compile("(\\d{4}-\\d{3}[\\dxX])(.*)");
	private static final Pattern ISSN_CLEAN = Pattern.compile("-");

	private ISSNUtils() {
	}

	/**
	 * @param rawDataField {@link DataField}, usually tag 022
	 * @return {@link Issn}
	 */
	public static Issn createIssn(final DataField rawDataField) {
		Subfield subfieldA = rawDataField.getSubfield('a');
		if (subfieldA == null) throw new NoDataException(IdentifierUtils.NO_SUBFIELD);

		Matcher matcherIssn = ISSN_PATTERN.matcher(subfieldA.getData());
		if (!matcherIssn.find()) throw new NoDataException(IdentifierUtils.NO_USABLE_DATA);
		if (!isValidLocal(matcherIssn.group(1))) throw new NumberFormatException(matcherIssn.group(1));

		List<String> notes = new ArrayList<>();
		notes.add(IdentifierUtils.parseNote(matcherIssn.group(2)));

		for (Subfield subfieldQ : rawDataField.getSubfields('q')) {
			notes.add(IdentifierUtils.parseNote(subfieldQ.getData()));
		}
		return Issn.create(matcherIssn.group(1), 1L, String.join(" ", notes));
	}

	/**
	 * @param issn String to be validated
	 * @return true or false
	 */
	public static boolean isValid(final String issn) {
		Matcher matcher = ISSN_PATTERN.matcher(issn);
		return matcher.find() && isValidLocal(matcher.group(1));
	}

	/**
	 * @param issn String to be validated
	 * @return valid issn as String or exception
	 */
	public static String getValidIssnThrowing(final String issn) {
		// empty string
		if (issn.isEmpty()) throw new NoDataException(IdentifierUtils.NO_USABLE_DATA);
		Matcher matcher = ISSN_PATTERN.matcher(issn);
		// bad string
		if (!matcher.find()) throw new NoDataException(IdentifierUtils.NO_USABLE_DATA);
		// valid issn
		if (isValidLocal(matcher.group(1))) return matcher.group(1);
		// not valid issn
		throw new NumberFormatException(issn);
	}

	/**
	 * @param issn String to be validated
	 * @return valid issn as String or null
	 */
	public static String getValidIssn(final String issn) {
		try {
			return getValidIssnThrowing(issn);
		} catch (NumberFormatException | NoDataException e) {
			return null;
		}
	}

	/**
	 * @param issn String to be validated, without regex control
	 * @return true or false
	 */
	private static boolean isValidLocal(final String issn) {
		String tempIssn = ISSN_CLEAN.matcher(issn).replaceAll("");
		int sum = 0;
		for (int i = 0; i < 8; i++) {
			if (tempIssn.charAt(i) == 'X') {
				sum += 10 * (8 - i);
			} else {
				sum += Character.getNumericValue(tempIssn.charAt(i)) * (8 - i);
			}
		}
		return (sum % 11 == 0);
	}

}
