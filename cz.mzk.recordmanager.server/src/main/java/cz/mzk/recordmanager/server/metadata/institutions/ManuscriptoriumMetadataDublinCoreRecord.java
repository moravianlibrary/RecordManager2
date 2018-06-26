package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.List;
import java.util.stream.Collectors;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class ManuscriptoriumMetadataDublinCoreRecord extends MetadataDublinCoreRecord{

	public ManuscriptoriumMetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		super(dcRecord);
	}

	@Override
	public List<String> getDefaultStatuses() {
		return SolrUtils.createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public List<String> getUrls() {
		List<String> urls = super.getUrls();
		if (urls == null) return null;
		return urls.stream().map(url -> Constants.DOCUMENT_AVAILABILITY_ONLINE + '|' + url).collect(Collectors.toList());
	}

}
