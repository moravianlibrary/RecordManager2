package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.regex.Matcher;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SkatMarcMetadataRecord extends MetadataMarcRecord {

	public SkatMarcMetadataRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public String getUUId() {
		String baseStr = underlayingMarc.getField("911", 'u');
		if (baseStr == null) {
			return null;
		}

		Matcher matcher = UUID_PATTERN.matcher(baseStr);
		if (matcher.find()) {
			String uuidStr = matcher.group(0);
			if (uuidStr != null && uuidStr.length() > 5) {
				return uuidStr.substring(5);
			}
		}
		return null;
	}
	
	/**
	 * filtered when there isn't any subfield q = 0
	 */
	@Override
	public boolean matchFilter() {
		for (DataField df : underlayingMarc.getDataFields("996")) {
			if (df.getSubfield('q') == null || !df.getSubfield('q').getData().equals("0")) {
				return true;
			}
		}
		return false;
	}

}
