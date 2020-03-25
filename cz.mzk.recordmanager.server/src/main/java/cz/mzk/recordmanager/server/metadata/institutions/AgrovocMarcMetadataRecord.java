package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;

public class AgrovocMarcMetadataRecord extends MetadataMarcRecord {

	public AgrovocMarcMetadataRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public TezaurusKey getTezaurusKey() {
		for (String field : new String[] { "150" }) {
			String value = underlayingMarc.getField(field, 'a');
			if (value != null) {
				return new TezaurusKey(field, value.toLowerCase());
			}
		}

		return null;
	}

}
