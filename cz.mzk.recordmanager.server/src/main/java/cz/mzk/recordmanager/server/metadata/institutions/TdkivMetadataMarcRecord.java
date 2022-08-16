package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;

import java.util.Collections;
import java.util.List;

public class TdkivMetadataMarcRecord extends MetadataMarcRecord {

	public TdkivMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.singletonList(HarvestedRecordFormatEnum.OTHER_DICTIONARY_ENTRY);
	}

	@Override
	public List<String> getUrls() {
		return super.getUrls(Constants.DOCUMENT_AVAILABILITY_NA, EVersionConstants.TDKIV_LINK);
	}
}
