package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class RkkaMetadataMarcRecord extends MetadataMarcRecord {

	public RkkaMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getAuthorAuthKey() {
		for (String tag : new String[]{"100", "700"}) {
			for (char subfield : new char[]{'7', '0'}) {
				String authKey = underlayingMarc.getField(tag, subfield);
				if (authKey != null) {
					return authKey;
				}
			}
		}
		return null;
	}

	@Override
	protected boolean isAudioCD() {
		if (super.isAudioCD()) return true;

		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());
		String f300 = underlayingMarc.getDataFields("300").toString();

		return MetadataUtils.containsChar(ARRAY_IJ, ldr06) && DIGITAL_OR_12CM.matcher(f300).find();
	}
}
