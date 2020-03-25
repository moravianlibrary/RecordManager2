package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.Collections;

public class SfxJibTechMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "http://sfx.techlib.cz/sfxlcl41";

	public SfxJibTechMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getSfxUrl(String id) {
		return super.generateSfxUrl(SFX_URL, id, Collections.singletonMap("sid", "cpk"));
	}

}
