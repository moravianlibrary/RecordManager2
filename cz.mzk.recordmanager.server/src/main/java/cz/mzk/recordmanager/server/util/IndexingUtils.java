package cz.mzk.recordmanager.server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class IndexingUtils {

	private static final Pattern OAI_RECORD_ID_PATTERN = Pattern.compile("oai:[\\w|.]+:([\\w|-]+)");

	public static String getSolrId(HarvestedRecord record) {
		String prefix = record.getHarvestedFrom().getIdPrefix();
		String suffix = record.getUniqueId().getRecordId();
		Matcher matcher = OAI_RECORD_ID_PATTERN.matcher(suffix);
		if (matcher.matches()) {
			suffix = matcher.group(1);
		}
		String id = ((prefix != null) ? prefix + "." : "") + suffix;
		return id;
	}

}
