package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class SfxTechlibMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "https://sfx.techlib.cz/sfxlcl3";

	public SfxTechlibMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getSfxUrl(String id) {
		return super.generateSfxUrl(SFX_URL, id, Collections.singletonMap("sfx.institute", getSfxInstitute()));
	}

}
