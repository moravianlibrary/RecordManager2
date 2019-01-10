package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public class KramMkpMetadataMarcRecord extends KramDefaultMetadataMarcRecord {

	public KramMkpMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		return generateUrl("http://kramerius4.mlp.cz/search/handle/");
	}

	@Override
	public boolean getIndexWhenMerged() {
		return true;
	}

}
