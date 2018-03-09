package cz.mzk.recordmanager.server.util.identifier;

import java.util.regex.Pattern;

public class EANUtils {

	private static final Pattern EAN_PATTERN = Pattern.compile("[0-9]{13}");
	
	public static boolean isEAN13valid(String ean) {
		if (EAN_PATTERN.matcher(ean).matches()) {
			int sumOdd = 0;
			int sumEven = 0;
			for (int i = 1; i < 13 ; i++) {
				if (i % 2 == 0) sumEven += Character.getNumericValue(ean.charAt(i));
				else sumOdd += Character.getNumericValue(ean.charAt(i));
			}
			
			sumOdd *= 3;
			int sum = sumEven + sumOdd + Character.getNumericValue(ean.charAt(0));
			if (sum % 10 == 0) return true;
		}
		
		return false;
	}
}
