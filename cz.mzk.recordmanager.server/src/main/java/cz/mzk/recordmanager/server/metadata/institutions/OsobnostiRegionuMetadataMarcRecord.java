package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class OsobnostiRegionuMetadataMarcRecord extends AuthMetadataMarcRecord{

	public OsobnostiRegionuMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public List<String> getDefaultStatuses() {
		return SolrUtils.createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public String getAuthorityRecordId() {
		return null;
	}

}
