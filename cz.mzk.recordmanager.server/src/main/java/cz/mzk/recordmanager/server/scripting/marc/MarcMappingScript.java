package cz.mzk.recordmanager.server.scripting.marc;

import java.util.Map;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.MappingScript;

public interface MarcMappingScript extends MappingScript<MarcRecord> {
	
	@Override
	public Map<String, Object> parse(MarcRecord record);

}
