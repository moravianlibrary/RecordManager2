package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class AnlMetadataMarcRecord extends NkpMarcMetadataRecord {

	public AnlMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getClusterId() {
		return null;
	}

	@Override
	public boolean isEod() {
		return false;
	}
}
