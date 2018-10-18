package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class KramDefaultMetadataMarcRecord extends
		MetadataMarcRecord {

	public KramDefaultMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getUUId() {
		return harvestedRecord.getUniqueId().getRecordId();
	}

	@Override
	public String getAuthorString() {
		String author = super.getAuthorString();
		return author == null ? underlayingMarc.getField("720", 'a') : author;
	}
}
