package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public class KramMzkMetadataMarcRecord extends KramDefaultMetadataMarcRecord {

	public KramMzkMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		return generateUrl("http://www.digitalniknihovna.cz/mzk/uuid/");
	}

}
