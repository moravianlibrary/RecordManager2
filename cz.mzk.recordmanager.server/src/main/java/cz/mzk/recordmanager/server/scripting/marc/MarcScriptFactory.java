package cz.mzk.recordmanager.server.scripting.marc;

import java.io.InputStream;

import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.MappingScriptFactory;

public interface MarcScriptFactory extends MappingScriptFactory<MarcFunctionContext> {
	
	@Override
	public MappingScript<MarcFunctionContext> create(InputStream... scripts);

}
