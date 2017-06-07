package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class LibraryMetadataMarcRecord extends MetadataMarcRecord {
	public LibraryMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		if (underlayingMarc.getDataFields("DEL") != null
				&& !underlayingMarc.getDataFields("DEL").isEmpty()) {
			return false;

		}
		return true;
	}

}
