package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class UzeiMetadataMarcRecord extends MetadataMarcRecord {

	public UzeiMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getUniqueId() {
		String id = underlayingMarc.getControlField("SYS");
		return (id != null) ? id : super.getUniqueId();
	}
}
