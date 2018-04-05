package cz.mzk.recordmanager.server.scripting;

import java.io.InputStream;

/**
 * Interface for factory creating mapping scripts.
 *
 * @param <T> metadata record type
 * @author xrosecky
 * @see cz.mzk.recordmanager.server.scripting.MappingScript<T>
 */
public interface MappingScriptFactory<T> {

	/**
	 * Create mapping scripts
	 *
	 * @param scripts as {@link InputStream}
	 * @return {@link MappingScript}
	 */
	MappingScript<T> create(InputStream... scripts);

}
