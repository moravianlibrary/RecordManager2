package cz.mzk.recordmanager.server.marc;

import org.marc4j.marc.DataField;

/**
 * Interface for filtering data fields - typically by indicators
 * 
 * 
 * @author xrosecky
 *
 */
public interface DataFieldMatcher {
	
	boolean matches(DataField field);

}
