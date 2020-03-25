package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class TreMetadataMarcRecord extends MetadataMarcRecord {

	public TreMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public boolean matchFilter() {
		return super.matchFilter() && !underlayingMarc.getDataFields("996").isEmpty();
	}
}
