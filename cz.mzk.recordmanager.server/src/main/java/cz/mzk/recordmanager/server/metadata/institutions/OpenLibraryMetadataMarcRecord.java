package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;

import java.util.Collections;
import java.util.List;

public class OpenLibraryMetadataMarcRecord extends MetadataMarcRecord {

	public OpenLibraryMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getDefaultStatuses() {
		return Collections.singletonList(Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public boolean matchFilter() {
		return super.matchFilter() && !underlayingMarc.getDataFields("856").isEmpty();
	}

}
