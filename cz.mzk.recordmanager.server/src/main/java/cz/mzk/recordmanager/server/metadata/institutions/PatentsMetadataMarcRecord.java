package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class PatentsMetadataMarcRecord extends MetadataMarcRecord{

	public PatentsMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		String f001 = underlayingMarc.getControlField("001");
		if (f001 == null) return Collections.emptyList();
		if (f001.endsWith("_B6")) {
			return Collections.singletonList(HarvestedRecordFormatEnum.PATENTS_PATENTS);
		}
		if (f001.endsWith("_A3")) {
			return Collections.singletonList(HarvestedRecordFormatEnum.PATENTS_PATENT_APPLICATIONS);
		}
		if (f001.endsWith("_U1")) {
			return Collections.singletonList(HarvestedRecordFormatEnum.PATENTS_UTILITY_MODELS);
		}
		return Collections.emptyList();	
	}

	@Override
	public List<String> getUrls() {
		return getUrls(Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public List<String> getDefaultStatuses() {
		return SolrUtils.createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}
	
}
