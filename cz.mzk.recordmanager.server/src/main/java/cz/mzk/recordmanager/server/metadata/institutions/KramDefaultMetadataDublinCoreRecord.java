package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.List;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class KramDefaultMetadataDublinCoreRecord extends MetadataDublinCoreRecord{

	public KramDefaultMetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		super(dcRecord);
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> list = super.getDetectedFormatList();
		if(!list.contains(HarvestedRecordFormatEnum.ELECTRONIC_SOURCE)){
			list.add(HarvestedRecordFormatEnum.ELECTRONIC_SOURCE);
		}
		return list;		
	}
}
