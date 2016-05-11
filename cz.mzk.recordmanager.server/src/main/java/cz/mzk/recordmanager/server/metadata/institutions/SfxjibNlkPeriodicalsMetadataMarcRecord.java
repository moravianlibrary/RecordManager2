package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class SfxjibNlkPeriodicalsMetadataMarcRecord extends SfxMetadataMarcRecord{

	public SfxjibNlkPeriodicalsMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> list = super.getDetectedFormatList();
		if(!list.contains(HarvestedRecordFormatEnum.PERIODICALS)){
			list.add(HarvestedRecordFormatEnum.PERIODICALS);
		}
		return list;		
	}
}
