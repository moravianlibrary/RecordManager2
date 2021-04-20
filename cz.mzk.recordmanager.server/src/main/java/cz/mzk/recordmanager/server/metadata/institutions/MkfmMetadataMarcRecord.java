package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.marc4j.marc.DataField;

public class MkfmMetadataMarcRecord extends MetadataMarcRecord {

	public MkfmMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public boolean matchFilter() {
		if (!super.matchFilter()) return false;
		boolean q0 = true;
		if (underlayingMarc.getDataFields("996").isEmpty()) return true;
		for (DataField df : underlayingMarc.getDataFields("996")) {
			if (df.getSubfield('q') == null || !df.getSubfield('q').getData().equals("0")) {
				q0 = false;
				break;
			}
		}
		return !q0;
	}
}
