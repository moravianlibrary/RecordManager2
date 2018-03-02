package cz.mzk.recordmanager.server.marc;

import java.util.List;
import java.util.Map;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.export.IOFormat;

public interface MarcRecord {
	
	String EMPTY_SEPARATOR = "";

	String getControlField(String tag);
	
	/**
	 * Get subfields of first field separated by separator
	 * 
	 * @param tag
	 * @param separator
	 * @param subfields
	 * @return
	 */
	String getField(String tag, String separator, char... subfields);
	
	
	/**
	 * Get subfields of first field separated by default separator (ie. space)
	 * 
	 * @param tag
	 * @param subfields
	 * @return
	 */
	String getField(String tag, char... subfields);
	
	/**
	 * Extract given field with subfields separated by separator
	 * 
	 * @param tag
	 * @param separator
	 * @param subfields
	 * @return
	 */
	List<String> getFields(String tag, String separator, char... subfields);
	
	/**
	 * Extract given field with subfields separated by separator filtered by matcher
	 * 
	 * @param tag
	 * @param matcher for filtering data fields
	 * @param separator
	 * @param subfields
	 * @return
	 */
	List<String> getFields(String tag, DataFieldMatcher matcher, String separator, char... subfields);

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
	List<String> getFields(String tag, DataFieldMatcher matcher, SubfieldExtractionMethod method, String separator,
						   char... subfields);

	/**
	 * Extract given subfield from field filtered by matcher
	 * 
	 * @param tag
	 * @param matcher for filtering data fields
	 * @param subfield
	 * @return
	 */
	default List<String> getFields(String tag, DataFieldMatcher matcher, char subfield) {
		return getFields(tag, matcher, EMPTY_SEPARATOR, subfield);
	}
	
	/**
	 * Extract given subfield from field
	 * 
	 * @param tag
	 * @param subfield
	 * @return
	 */
	default List<String> getFields(String tag, char subfield) {
		return getFields(tag, MatchAllDataFieldMatcher.INSTANCE, EMPTY_SEPARATOR, subfield);
	}
	
	Map<String, List<DataField>> getAllFields();
	
	/**
	 * return all {@link DataField} having given tag
	 * @param tag
	 * @return
	 */
	List<DataField> getDataFields(String tag);
	
	/**
	 * return all {@link ControlField} having given tag
	 * @param tag
	 * @return
	 */
	List<ControlField> getControlFields(String tag);
	
	/**
	 * return record {@link Leader}
	 * @return
	 */
	Leader getLeader();
	
	/**
	 * get all subfields of {@link DataField} with corresponding codes
	 * @param field
	 * @param codes
	 * @return {@link List} of matching {@link Subfield} objects
	 */
	List<Subfield> getSubfields(DataField field, char[] codes);
	
	String export(IOFormat iOFormat);

	void addDataField(String tag, char ind1, char ind2, String... subfields);
}
