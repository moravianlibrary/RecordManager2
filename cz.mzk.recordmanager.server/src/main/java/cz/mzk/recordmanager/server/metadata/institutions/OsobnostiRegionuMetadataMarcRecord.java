package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class OsobnostiRegionuMetadataMarcRecord extends AuthMetadataMarcRecord{

	public OsobnostiRegionuMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getAuthorityRecordId() {
		return null;
	}

}
