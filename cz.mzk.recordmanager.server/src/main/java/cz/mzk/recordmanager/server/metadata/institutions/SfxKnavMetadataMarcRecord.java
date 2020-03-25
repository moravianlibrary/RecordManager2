package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class SfxKnavMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "http://msfx.lib.cas.cz/sfxlcl3";

	public SfxKnavMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getSfxUrl(String id) {
		return generateSfxUrl(SFX_URL, id, Collections.emptyMap());
	}

}
