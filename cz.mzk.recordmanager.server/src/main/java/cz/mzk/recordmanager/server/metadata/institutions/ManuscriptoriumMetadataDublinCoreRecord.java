package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.List;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class ManuscriptoriumMetadataDublinCoreRecord extends MetadataDublinCoreRecord{

	protected static final Pattern HTTP_PATTERN = Pattern.compile("^http://kramerius.*");
	
	public ManuscriptoriumMetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		super(dcRecord);
	}

	@Override
	public List<String> getDefaultStatuses() {
		return SolrUtils.createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

}
