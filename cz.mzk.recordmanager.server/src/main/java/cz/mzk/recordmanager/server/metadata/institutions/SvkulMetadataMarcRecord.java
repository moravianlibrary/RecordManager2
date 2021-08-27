package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.ArrayList;
import java.util.List;

public class SvkulMetadataMarcRecord extends MetadataMarcRecord {

	public SvkulMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getBarcodes() {
		List<String> result = new ArrayList<>();
		super.getBarcodes().stream().forEach(bc -> result.add("31480" + bc));
		return result;
	}

}
