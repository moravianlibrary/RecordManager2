package cz.mzk.recordmanager.server.scripting.marc.function;

/**
 * Marker interface to mark classes containg function used by MarcDSL script
 * 
 * Function must have following signature:
 * 
 * public List<String> functionName(MarcRecord record);
 * 
 * or
 * 
 * public String functionName(MarcRecord record);
 * 
 */
public interface MarcRecordFunctions {

}
