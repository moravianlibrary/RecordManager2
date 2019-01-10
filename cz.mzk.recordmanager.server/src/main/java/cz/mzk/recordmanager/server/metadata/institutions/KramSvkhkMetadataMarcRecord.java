package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public class KramSvkhkMetadataMarcRecord extends KramDefaultMetadataMarcRecord {

	public KramSvkhkMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		return generateUrl("http://kramerius.svkhk.cz/search/handle/");
	}

	@Override
	public boolean getIndexWhenMerged() {
		return true;
	}

}
