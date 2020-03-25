package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SfxDirectMetadataMarcRecord extends MetadataMarcRecord {

	public SfxDirectMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getSfxUrl(String id) {
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null) return df.getSubfield('u').getData();
		}
		return null;
	}

}
