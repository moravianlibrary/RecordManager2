package cz.mzk.recordmanager.server.scripting;

import java.io.InputStream;

import cz.mzk.recordmanager.server.marc.MarcRecord;

public interface MarcScriptFactory extends MappingScriptFactory<MarcRecord> {
	
	@Override
	public MarcMappingScript create(InputStream... scripts);

}
