package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;

import java.util.List;

public class MunipressMetadataMarcRecord extends EbooksMetadataMarcRecord {

	public MunipressMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		return super.getUrls(Constants.DOCUMENT_AVAILABILITY_ONLINE, EVersionConstants.FULLTEXT_LINK);
	}
}
