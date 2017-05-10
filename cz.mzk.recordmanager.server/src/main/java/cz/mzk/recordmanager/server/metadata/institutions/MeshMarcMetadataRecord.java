package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;

public class MeshMarcMetadataRecord extends MetadataMarcRecord {

	public MeshMarcMetadataRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public TezaurusKey getTezaurusKey() {
		for (String field : new String[] { "150", "151", "155" }) {
			String value = underlayingMarc.getField(field, 'a');
			if (value != null) {
				return new TezaurusKey(field, value);
			}
		}

		return null;
	}

}
