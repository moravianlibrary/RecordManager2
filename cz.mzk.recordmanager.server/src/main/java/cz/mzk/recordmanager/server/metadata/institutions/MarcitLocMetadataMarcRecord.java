package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class MarcitLocMetadataMarcRecord extends MarcitMetadataMarcRecord {

	public MarcitLocMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getUniqueId() {
		String id = super.getUniqueId();
		if (id != null) return id;
		return "del" + underlayingMarc.getField("010", 'a');
	}

}
