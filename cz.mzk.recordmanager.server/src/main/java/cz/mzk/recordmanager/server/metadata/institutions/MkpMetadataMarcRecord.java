package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class MkpMetadataMarcRecord extends MetadataMarcRecord {

	public MkpMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean subjectFacet() {
		return false;
	}

	@Override
	public boolean genreFacet() {
		return false;
	}

	@Override
	public boolean matchFilter() {
		return !super.isArticle() || super.isArticle773();
	}
}
