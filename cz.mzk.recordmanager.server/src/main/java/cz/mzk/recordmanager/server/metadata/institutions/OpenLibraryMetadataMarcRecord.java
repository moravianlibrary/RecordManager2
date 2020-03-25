package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.List;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class OpenLibraryMetadataMarcRecord extends MetadataMarcRecord {

	public OpenLibraryMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getDefaultStatuses() {
		return SolrUtils.createHierarchicFacetValues(
				Constants.DOCUMENT_AVAILABILITY_ONLINE,
				Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public boolean matchFilter() {
		return super.matchFilter() && !underlayingMarc.getDataFields("856").isEmpty();
	}

}
