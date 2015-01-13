package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface DedupRecordLocator {

	public DedupRecord locate(HarvestedRecord record);
	
}
