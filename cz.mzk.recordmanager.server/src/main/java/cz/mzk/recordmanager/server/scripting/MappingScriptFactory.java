package cz.mzk.recordmanager.server.scripting;

import java.io.InputStream;

/**
 * 
 * Interface for factory creating mapping scripts.
 * 
 * @see cz.mzk.recordmanager.server.scripting.MappingScript<T>
 * 
 * @author xrosecky
 *
 * @param <T> metadata record type
 */
public interface MappingScriptFactory<T> {
	
	/**
	 * 
	 * Create 
	 * 
	 * 
	 * @param scripts
	 * @return
	 */
	MappingScript<T> create(InputStream... scripts);

}
