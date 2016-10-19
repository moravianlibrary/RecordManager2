package cz.mzk.recordmanager.server.marc;

import java.util.List;

import org.marc4j.marc.DataField;

public interface SubfieldJoiner {

	public List<String> extract(DataField df, String separator, char... subfields);

}
