package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Mapping resolver that caches translation/mapping files
 * 
 * @author xrosecky
 *
 */
public class CachingMappingResolver implements MappingResolver {
	
	private final MappingResolver delegate;
	
	private Map<String, Mapping> mappings = new ConcurrentHashMap<String, Mapping>();

	public CachingMappingResolver(MappingResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public Mapping resolve(String file) throws IOException {
		Mapping mapping = mappings.get(file);
		if (mapping != null) {
			return mapping;
		}
		mapping = delegate.resolve(file);
		mappings.put(file, mapping);
		return mapping;
	}

}
