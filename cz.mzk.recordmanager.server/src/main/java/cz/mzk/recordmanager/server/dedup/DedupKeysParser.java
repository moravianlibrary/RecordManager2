package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface DedupKeysParser {
	
	public List<String> getSupportedFormats();
	
	public HarvestedRecord parse(HarvestedRecord record) throws DedupKeyParserException;

}
