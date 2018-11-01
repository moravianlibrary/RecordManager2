package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public class KramNlkMetadataDublinCoreRecord extends
		KramDefaultMetadataDublinCoreRecord {

	public KramNlkMetadataDublinCoreRecord(DublinCoreRecord dcRecord,
										   HarvestedRecord hr) {
		super(dcRecord, hr);
	}

	@Override
	public List<String> getUrls() {
		return generateUrl("https://kramerius.medvik.cz/search/handle/");
	}

}
