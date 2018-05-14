package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class KkkvMetadataMarcRecord extends MetadataMarcRecord {

	public KkkvMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public String getUniqueId() {
		String id = underlayingMarc.getControlField("SYS");
		return (id != null) ? id : super.getUniqueId();
	}
}
