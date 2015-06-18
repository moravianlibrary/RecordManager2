package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class NkpMarcMetadataRecord extends MetadataMarcRecord {

	public NkpMarcMetadataRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public String getClusterId() {
		return underlayingMarc.getControlField("001");
	} 

}
