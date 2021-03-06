package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import org.marc4j.marc.DataField;

public class CosmotronUtils {

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
				return df.getSubfield('w').getData();
			}
			if (df.getIndicator1() == '0'
					&& df.getSubfield('7') != null
					&& df.getSubfield('7').getData().equals("nnas")
					&& df.getSubfield('w') != null) {
				return df.getSubfield('w').getData();
			}
		}
		return null;
	}

	public static boolean existsFields996(MarcRecord mr) {
		return !mr.getDataFields("996").isEmpty();
	}

}
