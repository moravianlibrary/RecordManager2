package cz.mzk.recordmanager.server.scripting;

import java.util.Map;

/**
 * Class responsible for extracting fields from metadata record for
 * indexing into Solr
 * 
 * @author xrosecky
 *
 * @param <T> Metadata record class
 */
public interface MappingScript<T> {

	public Map<String, Object> parse(T record);

}
