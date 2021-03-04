package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.Collections;

public class AnlMarcMetadataRecord extends MetadataMarcRecord {

	public AnlMarcMetadataRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public boolean isEdd() {
		return !Collections.disjoint(getDetectedFormatList(), EDD_FORMAT_ALLOWED);
	}
}
