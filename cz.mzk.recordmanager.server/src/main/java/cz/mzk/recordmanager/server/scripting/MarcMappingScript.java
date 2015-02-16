package cz.mzk.recordmanager.server.scripting;

import java.util.Map;

import cz.mzk.recordmanager.server.marc.MarcRecord;

public interface MarcMappingScript extends MappingScript<MarcRecord> {
	
	@Override
	public Map<String, Object> parse(MarcRecord record);

}
