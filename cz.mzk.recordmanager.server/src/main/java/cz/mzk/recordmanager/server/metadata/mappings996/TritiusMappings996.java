package cz.mzk.recordmanager.server.metadata.mappings996;

import org.marc4j.marc.DataField;

public class TritiusMappings996 extends DefaultMappings996 {

	@Override
	public String getCallnumber(DataField df) {
		return df.getSubfield('h') != null ? df.getSubfield('h').getData() : "";
	}

	@Override
	public String getLocation(DataField df) {
		return "";
	}

}
