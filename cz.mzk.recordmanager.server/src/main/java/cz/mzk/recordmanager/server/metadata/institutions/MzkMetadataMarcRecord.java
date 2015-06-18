package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class MzkMetadataMarcRecord extends MetadataMarcRecord {

	public MzkMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public String getClusterId() {
		String f001 = underlayingMarc.getControlField("001");
		if (!f001.matches("00.*")) {
			return f001;
		}
		return null;
	}
}
