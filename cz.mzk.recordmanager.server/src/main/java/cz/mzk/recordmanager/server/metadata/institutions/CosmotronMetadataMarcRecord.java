package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.util.CosmotronUtils;

public class CosmotronMetadataMarcRecord extends MetadataMarcRecord {

	public CosmotronMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		if (CosmotronUtils.getParentId(underlayingMarc) != null
				&& underlayingMarc.getDataFields("996").isEmpty()) {
			return false;
		}

		return true;
	}

}
