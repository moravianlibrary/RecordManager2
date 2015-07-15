package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;

/**
 *
 * Interface for resolving mapping files used for translation values before 
 * indexing to Solr (eg. language shortcut to full name).
 * In DSL they are referenced in the following way:
 * 
 * language = translate("mzk_language.map", getLanguages(), null)
 * 
 * Example of entry in mapping file:
 * 
 * eng = English
 * est = Estonian
 * 
 * @author xrosecky
 *
 */
public interface MappingResolver {

	public Mapping resolve(String file) throws IOException;

}
