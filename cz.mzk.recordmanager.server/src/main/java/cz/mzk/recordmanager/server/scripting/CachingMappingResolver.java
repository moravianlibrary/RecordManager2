package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
	
	private final List<MappingResolver> delegates;
	
	private Map<String, Mapping> mappings = new ConcurrentHashMap<String, Mapping>();

	public CachingMappingResolver(MappingResolver... delegates) {
		this.delegates = Arrays.asList(delegates);
	}

	@Override
	public Mapping resolve(String file) throws IOException {
		Mapping mapping = mappings.get(file);
		if (mapping != null) {
			return mapping;
		}
		for (MappingResolver delegate : delegates) {
			try {
				mapping = delegate.resolve(file);
				break;
			} catch (IllegalArgumentException iae) {
				continue;
			}
		}
		if (mapping == null) {
			throw new IllegalArgumentException(String.format("File %s not found", file));
		}
		mappings.put(file, mapping);
		return mapping;
	}

}
