package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class BmcMetadataMarcRecord extends MetadataMarcRecord{
	
	private static final String SUBJECT_FACET_FILE = "subject_facet_bmc.txt";
	private static final String LINK_STR = "%s|https://www.medvik.cz/bmc/link.do?id=%s|záznam v BMČ";

	public BmcMetadataMarcRecord(MarcRecord underlayingMarc){
		super(underlayingMarc);		
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		results.addAll(super.getUrls());

		if (underlayingMarc.getControlField("001") != null) {
			results.add(String.format(LINK_STR, Constants.DOCUMENT_AVAILABILITY_UNKNOWN, underlayingMarc.getControlField("001")));
		}
		return results;
	}

	@Override
	public String filterSubjectFacet() {
		return SUBJECT_FACET_FILE;
	}
}
