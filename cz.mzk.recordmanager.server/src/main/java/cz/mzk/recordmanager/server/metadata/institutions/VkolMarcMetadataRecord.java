package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class VkolMarcMetadataRecord extends MetadataMarcRecord {

	public VkolMarcMetadataRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public String getClusterId() {
		return underlayingMarc.getControlField("001");
	} 

}
