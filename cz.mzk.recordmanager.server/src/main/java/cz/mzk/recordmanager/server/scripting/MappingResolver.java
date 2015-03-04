package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;

public interface MappingResolver {
	
	public Mapping resolve(String file) throws IOException;

}
