package cz.mzk.recordmanager.server.scripting;

import java.io.InputStream;

public interface MappingScriptFactory<T> {
	
	public MappingScript<T> create(InputStream... scripts);

}
