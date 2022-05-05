package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;
import cz.mzk.recordmanager.server.util.CleaningUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MarcitLocMetadataMarcRecord extends MarcitMetadataMarcRecord {

	public MarcitLocMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	public static final Pattern CLEAN_LOC = Pattern.compile("[\\s-]");
	public static final String LOC_FOR_SEARCH = "search";

	@Override
	public String getUniqueId() {
		String id = super.getUniqueId();
		if (id != null) return id;
		return "del" + underlayingMarc.getField("010", 'a');
	}

	@Override
	public List<Loc> getLocIds() {
		List<Loc> results = new ArrayList<>();
		for (Loc loc : getLocIds(new char[]{'a'})) {
			results.add(loc);
			results.add(Loc.create(CleaningUtils.replaceAll(loc.getLoc(), CLEAN_LOC, ""), "search"));
		}
		results.addAll(getLocIds(new char[]{'z'}));
		return results;
	}

}
