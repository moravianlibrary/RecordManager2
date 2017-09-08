package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.regex.Pattern;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SfxjibNlkMetadataMarcRecord extends MetadataMarcRecord {

	private static final Pattern URL = Pattern
			.compile("http:\\/\\/www.medvik.cz\\/link\\/access.do");

	public SfxjibNlkMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null
					&& URL.matcher(df.getSubfield('u').getData()).find()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getSfxUrl(String id) {
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null) return df.getSubfield('u').getData();
		}
		return null;
	}

}
