package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;
import liquibase.pro.packaged.S;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.ArrayList;
import java.util.List;

public class MarcitMetadataMarcRecord extends MetadataMarcRecord {

	public MarcitMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<Loc> getLocIds() {
		List<Loc> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("010")) {
			for (char code : new char[]{'a', 'z'}) {
				for (Subfield sf : df.getSubfields(code)) {
					results.add(Loc.create(sf.getData(), code));
				}
			}
		}
		return results;
	}

}
