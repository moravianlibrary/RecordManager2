package cz.mzk.recordmanager.server.metadata.mappings996;

import org.marc4j.marc.DataField;

public class AlephMappings996 extends DefaultMappings996 {

	@Override
	public String getItemId(DataField df) {
		return df.getSubfield('w') != null ? df.getSubfield('w').getData() : "";
	}

	@Override
	public String getAgencyId(DataField df) {
		return df.getSubfield('j') != null ? df.getSubfield('j').getData().toUpperCase() : "";
	}

	@Override
	public String getSequenceNo(DataField df) {
		return df.getSubfield('u') != null ? df.getSubfield('u').getData() : "";
	}
}
