package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import org.marc4j.marc.DataField;

import java.util.regex.Pattern;

public class CosmotronUtils {

	private static final Pattern PATTERN_REPLACE_ID = Pattern.compile("_us_cat\\*");
	private static final String ID_REPLACEMENT = "UsCat" + Constants.COSMOTRON_RECORD_ID_CHAR;

	public static String getParentId(MarcRecord mr) {
		String leader = mr.getLeader().toString();
		if (leader.length() >= 8 && leader.substring(6, 8).equals("aa")) {
			// article, not record with 996 for periodicals
			return null;
		}
		for (DataField df : mr.getDataFields("773")) {
			if (df.getIndicator1() == '0'
					&& df.getIndicator2() == '8'
					&& df.getSubfield('w') != null) {
				return parseIdFrom773(df.getSubfield('w').getData());
			}
			if (df.getIndicator1() == '0'
					&& df.getSubfield('7') != null
					&& df.getSubfield('7').getData().equals("nnas")
					&& df.getSubfield('w') != null) {
				return parseIdFrom773(df.getSubfield('w').getData());
			}
		}
		return null;
	}

	private static String parseIdFrom773(String id) {
		id = Character.toUpperCase(id.charAt(0)) + id.substring(1);
		id = CleaningUtils.replaceAll(id, PATTERN_REPLACE_ID, ID_REPLACEMENT);
		return id;
	}

	public static boolean existsFields996(MarcRecord mr) {
		return !mr.getDataFields("996").isEmpty();
	}

}
