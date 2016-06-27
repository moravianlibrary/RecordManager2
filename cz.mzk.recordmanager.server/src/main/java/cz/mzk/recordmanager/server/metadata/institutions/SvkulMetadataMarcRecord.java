package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SvkulMetadataMarcRecord extends MetadataMarcRecord{

	public SvkulMetadataMarcRecord(MarcRecord underlayingMarc){
		super(underlayingMarc);		
	}
	
	@Override
	public List<String> getBarcodes(){
		List<String> result = new ArrayList<>();
		super.getBarcodes().stream().forEach(bc -> result.add("31480"+bc));
		return result;
	}
}
