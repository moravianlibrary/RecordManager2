package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class PatentsMetadataMarcRecord extends MetadataMarcRecord{

	public PatentsMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> list = super.getDetectedFormatList();
		if(!list.contains(HarvestedRecordFormatEnum.PATENTS)){
			list.add(HarvestedRecordFormatEnum.PATENTS);
		}
		return list;	
	}

}
