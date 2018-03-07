package cz.mzk.recordmanager.server.util.identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ISSNUtils {

	private static final Pattern ISSN_PATTERN = Pattern.compile("\\d{4}-\\d{3}[\\dxX]");

	public static boolean isValid(String issn) {
		Matcher matcher = ISSN_PATTERN.matcher(issn);
		if (!matcher.find()) {
			return false;
		}
		issn = issn.replaceAll("-", "");
		int sum = 0;
		for (int i = 0; i < 8; i++){
			if (issn.charAt(i) == 'X'){
				sum += 10 * (8-i);
			} else {
				sum += Character.getNumericValue(issn.charAt(i)) * (8-i);
			}
		}
		return (sum % 11 == 0);
	}

}
