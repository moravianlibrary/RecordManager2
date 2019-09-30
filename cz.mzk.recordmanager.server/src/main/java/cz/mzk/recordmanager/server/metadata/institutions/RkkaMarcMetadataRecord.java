package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class RkkaMarcMetadataRecord extends MetadataMarcRecord {

	public RkkaMarcMetadataRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	protected boolean isAudioCD() {
		if (super.isAudioCD()) return true;

		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());
		String f300 = underlayingMarc.getDataFields("300").toString();

		return MetadataUtils.containsChar(ARRAY_IJ, ldr06) && DIGITAL_OR_12CM.matcher(f300).find();
	}
}
