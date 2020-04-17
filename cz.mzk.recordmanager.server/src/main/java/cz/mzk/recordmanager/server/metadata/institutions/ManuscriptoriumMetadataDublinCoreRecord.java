package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class ManuscriptoriumMetadataDublinCoreRecord extends MetadataDublinCoreRecord{

	public ManuscriptoriumMetadataDublinCoreRecord(DublinCoreRecord dcRecord, HarvestedRecord hr) {
		super(dcRecord, hr);
	}

	@Override
	public List<String> getDefaultStatuses() {
		return Collections.singletonList(Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public List<String> getUrls() {
		List<String> urls = super.getUrls();
		if (urls == null) return null;
		return urls.stream().map(url -> MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
				Constants.DOCUMENT_AVAILABILITY_ONLINE, url, "")).collect(Collectors.toList());
	}

}
