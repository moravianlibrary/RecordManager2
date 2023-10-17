package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.CleaningUtils;

import java.util.regex.Pattern;

public class CnbMetadataMarcRecord extends MetadataMarcRecord {

	public CnbMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	private static final Pattern TITLE = Pattern.compile("\\[(.*?)\\]");

	@Override
	protected String filterTitleValue(String title) {
		return CleaningUtils.replaceAll(title, TITLE, "");
	}
}
