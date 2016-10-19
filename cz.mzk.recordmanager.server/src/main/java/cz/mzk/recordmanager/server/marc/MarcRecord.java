package cz.mzk.recordmanager.server.marc;

import java.util.List;
import java.util.Map;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.export.IOFormat;

public interface MarcRecord {
	
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
	 * Extract given field with subfields separated by separator filtered by matcher
	 *
	 * @param tag
	 * @param matcher for filtering data fields
	 * @param method for subfields extraction
	 * @param separator
	 * @param subfields
	 * @return
	 */
	public List<String> getFields(String tag, DataFieldMatcher matcher, SubfieldExtractionMethod method, String separator,
			char... subfields);

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
	
	/**
	 * return all {@link DataField} having given tag
	 * @param tag
	 * @return
	 */
	public List<DataField> getDataFields(String tag);
	
	/**
	 * return all {@link ControlField} having given tag
	 * @param tag
	 * @return
	 */
	public List<ControlField> getControlFields(String tag);
	
	/**
	 * return record {@link Leader}
	 * @return
	 */
	public Leader getLeader();
	
	/**
	 * get all subfields of {@link DataField} with corresponding codes
	 * @param field
	 * @param codes
	 * @return {@link List} of matching {@link Subfield} objects
	 */
	public List<Subfield> getSubfields(DataField field, char[] codes);
	
	public String export(IOFormat iOFormat);

	public void addDataField(String tag, char ind1, char ind2, String... subfields);
}
