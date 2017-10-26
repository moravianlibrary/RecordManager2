package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class TreMetadataMarcRecord extends MetadataMarcRecord{

	public TreMetadataMarcRecord(MarcRecord underlayingMarc){
		super(underlayingMarc);		
	}
	
	@Override
	public boolean matchFilter(){
		if (!super.matchFilter()) return false;
		if(underlayingMarc.getDataFields("996").isEmpty()) return false;
		else return true;
	}
}
