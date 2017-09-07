package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;

public class TdkivMetadataMarcRecord extends MetadataMarcRecord {

	public TdkivMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.singletonList(HarvestedRecordFormatEnum.OTHER_DICTIONARY_ENTRY);
	}

	@Override
	public List<String> getUrls() {
		return super.getUrls(Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}
}
