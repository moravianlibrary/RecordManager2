package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class TreMetadataMarcRecord extends MetadataMarcRecord {

	public TreMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		return super.matchFilter() && !underlayingMarc.getDataFields("996").isEmpty();
	}
}
