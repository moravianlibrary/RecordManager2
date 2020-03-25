package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.CitationRecordType;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.BLTopicKey;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class MzkNormsMetadataMarcRecord extends MetadataMarcRecord{

	public MzkNormsMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.singletonList(HarvestedRecordFormatEnum.NORMS);		
	}

	@Override
	public CitationRecordType getCitationFormat() {
		return CitationRecordType.NORMS;
	}

	/**
	 * 084a
	 *
	 * @return List of {@link BLTopicKey}
	 */
	@Override
	public List<BLTopicKey> getBiblioLinkerTopicKey() {
		List<BLTopicKey> result = new ArrayList<>();
		for (String value : underlayingMarc.getFields("084", 'a')) {
			result.add(BLTopicKey.create(value));
		}
		return result;
	}
}
