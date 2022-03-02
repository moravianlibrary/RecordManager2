package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class OpenLibraryMetadataMarcRecord extends EbooksMetadataMarcRecord {

	public OpenLibraryMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public boolean matchFilter() {
		return super.matchFilter() && !underlayingMarc.getDataFields("856").isEmpty();
	}

}
