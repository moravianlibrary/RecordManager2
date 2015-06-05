package cz.mzk.recordmanager.server.scripting.dc;

import java.util.Map;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.scripting.MappingScript;

public interface DublinCoreMappingScript extends MappingScript<DublinCoreRecord> {
	
	@Override
	public Map<String, Object> parse(DublinCoreRecord record);

}
