package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class VkolMarcMetadataRecord extends MetadataMarcRecord {

	public VkolMarcMetadataRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}
	
	@Override
	public String getClusterId() {
		return underlayingMarc.getControlField("001");
	} 

}
