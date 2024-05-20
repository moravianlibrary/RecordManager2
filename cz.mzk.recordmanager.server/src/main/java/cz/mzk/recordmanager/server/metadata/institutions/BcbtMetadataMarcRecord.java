package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BcbtMetadataMarcRecord extends MetadataMarcRecord {

	public BcbtMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	private static final List<HarvestedRecordFormatEnum> AUDIO_VIDEO = Arrays.asList(
			HarvestedRecordFormatEnum.AUDIO_CASSETTE,
			HarvestedRecordFormatEnum.AUDIO_CD,
			HarvestedRecordFormatEnum.AUDIO_DVD,
			HarvestedRecordFormatEnum.AUDIO_LP,
			HarvestedRecordFormatEnum.AUDIO_DOCUMENTS,
			HarvestedRecordFormatEnum.AUDIO_OTHER,
			HarvestedRecordFormatEnum.VIDEO_BLURAY,
			HarvestedRecordFormatEnum.VIDEO_CD,
			HarvestedRecordFormatEnum.VIDEO_DVD,
			HarvestedRecordFormatEnum.VIDEO_OTHER,
			HarvestedRecordFormatEnum.VIDEO_DOCUMENTS,
			HarvestedRecordFormatEnum.VIDEO_VHS
	);

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> results = super.getDetectedFormatList();
		if (!Collections.disjoint(results, AUDIO_VIDEO))
			return Collections.singletonList(HarvestedRecordFormatEnum.BOOKS);
		return results;
	}
}
