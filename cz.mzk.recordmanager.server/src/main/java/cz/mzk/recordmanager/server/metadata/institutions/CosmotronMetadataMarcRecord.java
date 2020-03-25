package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.CosmotronUtils;

public class CosmotronMetadataMarcRecord extends MetadataMarcRecord {

	public CosmotronMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public boolean matchFilter() {
		return CosmotronUtils.getParentId(underlayingMarc) == null
				|| !underlayingMarc.getDataFields("996").isEmpty();
	}

	@Override
	protected boolean isArticle773() {
		String leader = underlayingMarc.getLeader().toString();
		return leader.length() >= 8 && leader.substring(6, 8).equals("aa") && super.isArticle773();
	}
}
