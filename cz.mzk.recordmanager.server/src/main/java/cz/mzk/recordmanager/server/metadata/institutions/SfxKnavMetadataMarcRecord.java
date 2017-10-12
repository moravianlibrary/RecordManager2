package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.HashMap;
import java.util.Map;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.util.UrlUtils;

public class SfxKnavMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "http://msfx.lib.cas.cz/sfxlcl3";

	public SfxKnavMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public Boolean getMetaproxyBool() {
		return false;
	}

	@Override
	public String getSfxUrl(String id) {
		Map<String, String> allParams = new HashMap<>();
		allParams.put("url_ver", "Z39.88-2004");
		allParams.put("sfx.ignore_date_threshold", "1");
		allParams.put("rft.object_id", id);

		return UrlUtils.buildUrl(SFX_URL, allParams);
	}

}
