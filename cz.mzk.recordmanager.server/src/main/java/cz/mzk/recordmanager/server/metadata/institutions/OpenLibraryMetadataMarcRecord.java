package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.intercepting.OpenlibMarcInterceptor;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;

import java.util.Collections;
import java.util.List;

public class OpenLibraryMetadataMarcRecord extends EbooksMetadataMarcRecord {

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

	@Override
	public List<String> getUrls() {
		return super.getUrls(Constants.DOCUMENT_AVAILABILITY_UNKNOWN, OpenlibMarcInterceptor.TEXT_856y);
	}

}
