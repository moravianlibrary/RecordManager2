package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SfxMetadataMarcRecord extends MetadataMarcRecord{

	public SfxMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

}
