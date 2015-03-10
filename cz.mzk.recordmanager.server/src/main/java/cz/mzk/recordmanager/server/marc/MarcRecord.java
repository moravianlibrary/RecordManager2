package cz.mzk.recordmanager.server.marc;

import java.util.List;
import java.util.Map;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;

public interface MarcRecord extends MetadataRecord {
	
	public static final String EMPTY_SEPARATOR = "";

	public String getControlField(String tag);
	
	/**
	 * Get subfields of first field separated by separator
	 * 
	 * @param tag
	 * @param separator
	 * @param subfields
	 * @return
	 */
	public String getField(String tag, String separator, char... subfields);
	
	
	/**
	 * Get subfields of first field separated by default separator (ie. space)
	 * 
	 * @param tag
	 * @param separator
	 * @param subfields
	 * @return
	 */
	public String getField(String tag, char... subfields);
	
	/**
	 * Extract given field with subfields separated by separator
	 * 
	 * @param tag
	 * @param separator
	 * @param subfields
	 * @return
	 */
	public List<String> getFields(String tag, String separator, char... subfields);
	
	/**
	 * Extract given field with subfields separated by separator filtered by matcher
	 * 
	 * @param tag
	 * @param matcher for filtering data fields
	 * @param separator
	 * @param subfields
	 * @return
	 */
	public List<String> getFields(String tag, DataFieldMatcher matcher, String separator, char... subfields);
	
	/**
	 * Extract given subfield from field filtered by matcher
	 * 
	 * @param tag
	 * @param matcher for filtering data fields
	 * @param subfield
	 * @return
	 */
	default public List<String> getFields(String tag, DataFieldMatcher matcher, char subfield) {
		return getFields(tag, matcher, EMPTY_SEPARATOR, subfield);
	}
	
	/**
	 * Extract given subfield from field
	 * 
	 * @param tag
	 * @param subfield
	 * @return
	 */
	default public List<String> getFields(String tag, char subfield) {
		return getFields(tag, MatchAllDataFieldMatcher.INSTANCE, EMPTY_SEPARATOR, subfield);
	}
	
	public Map<String, List<DataField>> getAllFields();
	
}
