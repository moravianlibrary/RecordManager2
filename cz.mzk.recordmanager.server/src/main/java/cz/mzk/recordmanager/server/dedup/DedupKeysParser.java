package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupKeys;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface DedupKeysParser {
	
	public List<String> getSupportedFormats();
	
	public DedupKeys parse(HarvestedRecord record);

}
