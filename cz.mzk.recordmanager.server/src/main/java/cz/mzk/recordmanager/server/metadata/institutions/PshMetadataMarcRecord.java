package cz.mzk.recordmanager.server.metadata.institutions;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class PshMetadataMarcRecord extends MetadataMarcRecord {

	public PshMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public String getTezaurus() {
		for (DataField df : underlayingMarc.getDataFields("150")) {
			if (df.getSubfield('a') != null) {
				return df.getSubfield('a').getData();
			}
		}
		return null;
	}

}
