package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.Set;

public interface StopWordsResolver {

	public Set<String> resolve(String file) throws IOException;

}
