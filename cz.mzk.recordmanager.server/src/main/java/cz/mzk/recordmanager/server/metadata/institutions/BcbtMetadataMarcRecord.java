package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BcbtMetadataMarcRecord extends MetadataMarcRecord {

	public BcbtMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	private static final String LINK_BCBT = "https://knihoveda.lib.cas.cz/Record/%s";

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

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>(super.getUrls());

		if (underlayingMarc.getControlField("001") != null) {
			EVersionUrl url = EVersionUrl.create(harvestedRecord.getHarvestedFrom().getIdPrefix(),
					Constants.DOCUMENT_AVAILABILITY_UNKNOWN,
					String.format(LINK_BCBT, underlayingMarc.getControlField("001")), EVersionConstants.BCBT_LINK);
			if (url != null) results.add(url.toString());
		}

		return results;
	}
}
