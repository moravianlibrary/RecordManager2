package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class AuthMetadataMarcRecord extends MetadataMarcRecord{

	public AuthMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public boolean matchFilter() {
		if(underlayingMarc.getDataFields("100").isEmpty()) return false;
		
		for(DataField df: underlayingMarc.getDataFields("100")){
			if(df.getSubfield('t') != null) return false;
		}
		
		return true;
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.singletonList(HarvestedRecordFormatEnum.PERSON);
		
	}

}
