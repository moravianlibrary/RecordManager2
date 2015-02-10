package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;

public interface MarcRecord extends MetadataRecord {

	public String getField(String tag, char subfield);
}
