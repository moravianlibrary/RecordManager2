package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.HashMap;
import java.util.Map;

public class SfxJibMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SFX_URL = "https://sfx.knihovny.cz/sfxlcl3";

	public SfxJibMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	private static final Map<String, String> SFX_PREFIX = new HashMap<>();

	static {
		SFX_PREFIX.put("FREE", "ANY");
		SFX_PREFIX.put("SVKOS", "MSVK");
		SFX_PREFIX.put("CBVK", "JVKCB");
		SFX_PREFIX.put("KVKL", "KVKLI");
		SFX_PREFIX.put("KFBZ", "KKFB");
		SFX_PREFIX.put("SVKPK", "SVKPL");
		SFX_PREFIX.put("MKHK", "KMHK");
		SFX_PREFIX.put("KKVY", "KKVysociny");
	}

	protected Map<String, String> params = new HashMap<>();

	{
		params.put("sid", "cpk");
		params.put("sfx.institute", getSfxInstitute(SFX_PREFIX));
	}

	@Override
	public String getSfxUrl(String id) {
		return super.generateSfxUrl(SFX_URL, id, params);
	}

}
