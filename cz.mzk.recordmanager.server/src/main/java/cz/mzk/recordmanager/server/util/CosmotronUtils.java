package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import org.marc4j.marc.DataField;

public class CosmotronUtils {

	public static String getParentId(MarcRecord mr) {
		String leader = mr.getLeader().toString();
		if (leader.length() >= 8 && leader.substring(6,8).equals("aa")){
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

	private static String parseIdFrom773(String s) {
		s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
		s = s.replaceAll("_us_cat\\*", "UsCat" + Constants.COSMOTRON_RECORD_ID_CHAR);
		return s;
	}

}
