package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

import java.util.Collections;

public class SfxJibIrelMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "http://sfx.jib.cz/sfxirel";

	public SfxJibIrelMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public String getSfxUrl(String id) {
		return super.generateSfxUrl(SFX_URL, id, Collections.singletonMap("sid", "cpk"));
	}

}
