package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.CitationRecordType;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class MzkNormsMetadataMarcRecord extends MetadataMarcRecord{

	public MzkNormsMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.singletonList(HarvestedRecordFormatEnum.NORMS);		
	}

	@Override
	public CitationRecordType getCitationFormat() {
		return CitationRecordType.NORMS;
	}

}
