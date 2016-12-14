package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class BmcMetadataMarcRecord extends MetadataMarcRecord{
	
	private static final String SUBJECT_FACET_FILE = "subject_facet_bmc.txt";
	
	public BmcMetadataMarcRecord(MarcRecord underlayingMarc){
		super(underlayingMarc);		
	}

	@Override
	public String filterSubjectFacet() {
		return SUBJECT_FACET_FILE;
	}
}
