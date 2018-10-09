package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;

import java.util.Collections;
import java.util.List;

public class KramMzkMetadataMarcRecord extends
		KramDefaultMetadataMarcRecord {

	public KramMzkMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		String policy = Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		return Collections.singletonList(policy + '|'
				+ "http://www.digitalniknihovna.cz/mzk/uuid/"
				+ harvestedRecord.getUniqueId().getRecordId() + '|');
	}

	@Override
	public boolean getIndexWhenMerged() {
		return false;
	}

}
