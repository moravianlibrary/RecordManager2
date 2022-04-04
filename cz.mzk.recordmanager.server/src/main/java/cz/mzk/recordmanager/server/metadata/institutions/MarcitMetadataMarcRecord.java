package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;
import cz.mzk.recordmanager.server.util.CleaningUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MarcitMetadataMarcRecord extends MetadataMarcRecord {

	public MarcitMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	private static final Pattern LOC_ID = Pattern.compile("[-\\s0-9]*");

	@Override
	public List<Loc> getLocIds() {
		List<Loc> results = new ArrayList<>();
		for (String id : underlayingMarc.getFields("010", 'a')) {
			if (LOC_ID.matcher(id).matches()) {
				results.add(Loc.create(CleaningUtils.replaceAll(id, Pattern.compile("[-\\s]"), "")));
			}
		}
		return results;
	}

}
