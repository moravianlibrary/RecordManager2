package cz.mzk.recordmanager.server.metadata.mappings996;

import org.marc4j.marc.DataField;

public class DawinciMappings996 extends DefaultMappings996 {

	@Override
	public String getItemId(DataField df) {
		return df.getSubfield('a') != null ? df.getSubfield('a').getData() : "";
	}

}
