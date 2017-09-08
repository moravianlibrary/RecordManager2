package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SfxTechlibMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "http://sfx.techlib.cz/sfxlcl3";

	public SfxTechlibMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public Boolean getMetaproxyBool() {
		return false;
	}

	@Override
	public String getSfxUrl(String id) {
		return super.generateSfxUrl(SFX_URL, id, Collections.emptyMap());
	}

}
