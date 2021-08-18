package cz.mzk.recordmanager.server.metadata.mappings996;

import org.marc4j.marc.DataField;

import java.util.List;

public interface Mappings996 {

	boolean ignore(DataField df);

	String getItemId(DataField df);

	String getCallnumber(DataField df);

	String getDepartment(DataField df);

	String getLocation(DataField df);

	String getDescription(DataField df);

	String getNotes(DataField df);

	String getYear(DataField df);

	String getVolume(DataField df);

	String getIssue(DataField df);

	String getAvailability(DataField df);

	String getCollectionDesc(DataField df);

	String getAgencyId(DataField df);

	String getSequenceNo(DataField df);

	String getSubfieldW(DataField df);

	String getMappingAsCsv(DataField df);

	List<String> getMappingAsCsv(List<DataField> df);

}
