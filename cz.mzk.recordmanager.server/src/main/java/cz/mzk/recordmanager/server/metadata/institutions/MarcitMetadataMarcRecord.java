package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;

import java.util.ArrayList;
import java.util.List;

public class MarcitMetadataMarcRecord extends MetadataMarcRecord {

	public MarcitMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<Loc> getLocIds() {
		List<Loc> results = new ArrayList<>();
		for (String id : underlayingMarc.getFields("010", 'a')) {
			results.add(Loc.create(id));
		}
		return results;
	}

}
