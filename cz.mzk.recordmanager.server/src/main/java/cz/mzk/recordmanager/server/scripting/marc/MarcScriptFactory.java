package cz.mzk.recordmanager.server.scripting.marc;

import java.io.InputStream;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.MappingScriptFactory;

public interface MarcScriptFactory extends MappingScriptFactory<MarcRecord> {
	
	@Override
	public MarcMappingScript create(InputStream... scripts);

}
