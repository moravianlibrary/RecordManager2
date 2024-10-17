package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

import java.util.Collections;
import java.util.List;

import static cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum.*;

public class NkpMarcMetadataRecord extends MetadataMarcRecord {

	public NkpMarcMetadataRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getClusterId() {
		return underlayingMarc.getControlField("001");
	}

	private List<HarvestedRecordFormatEnum> getMusicalScoresFormat() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());
		if (ldr06 == 'c') {
			return Collections.singletonList(MUSICAL_SCORES_PRINTED);
		} else if (ldr06 == 'd') {
			return Collections.singletonList(MUSICAL_SCORES_MANUSCRIPT);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<HarvestedRecordFormatEnum> getNkpRecordFormats() {
		List<HarvestedRecordFormatEnum> results = super.getDetectedFormatList();
		results.remove(MUSICAL_SCORES);
		results.addAll(getMusicalScoresFormat());
		return results;
	}
}
