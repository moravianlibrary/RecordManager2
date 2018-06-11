package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import org.marc4j.marc.DataField;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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
		if (!super.matchFilter()) return false;
		for (DataField df : underlayingMarc.getDataFields("996")) {
			if (df.getSubfield('q') == null || !df.getSubfield('q').getData().equals("0")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<String> getCaslinSiglas() {
		return underlayingMarc.getFields("996", 'e').stream().map(String::trim).collect(Collectors.toSet());
	}
}
