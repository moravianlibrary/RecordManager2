package cz.mzk.recordmanager.server.metadata.mappings996;

import org.marc4j.marc.DataField;

public class CaslinMappings996 extends DefaultMappings996 {

	@Override
	public String getDepartment(DataField df) {
		return df.getSubfield('e') != null ? df.getSubfield('e').getData() : "";
	}
}
