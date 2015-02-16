package cz.mzk.recordmanager.server.scripting;

import java.util.Map;

public interface MappingScript<T> {
	
	public Map<String, Object> parse(T record);

}
