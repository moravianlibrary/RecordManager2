package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface HarvestedRecordDedupMatcher {
	
	/**
	 * Decide whether two given {@link HarvestedRecord} objects should be considered as duplicates.
	 * @param origRecord
	 * @param dedupRecord
	 * @return
	 */
	public boolean matchRecords(final HarvestedRecord record1, final HarvestedRecord record2);
}
