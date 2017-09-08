package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SfxJibMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "http://sfx.jib.cz/sfxlcl3";

	public SfxJibMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public Boolean getMetaproxyBool() {
		return false;
	}

	@Override
	public String getSfxUrl(String id) {
		return super.generateSfxUrl(SFX_URL, id, Collections.singletonMap("sid", "cpk"));
	}

}
