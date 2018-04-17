package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.Set;

public interface ListResolver {

	Set<String> resolve(String file) throws IOException;

}
