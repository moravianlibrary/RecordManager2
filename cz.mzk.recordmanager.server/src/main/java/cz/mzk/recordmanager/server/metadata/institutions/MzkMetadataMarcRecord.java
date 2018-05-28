package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

import java.util.regex.Pattern;

public class MzkMetadataMarcRecord extends MetadataMarcRecord {

	private static final Pattern CLUSTER_ID_PATTERN = Pattern.compile("00.*");

	public MzkMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public String getClusterId() {
		String f001 = underlayingMarc.getControlField("001");
		if (!CLUSTER_ID_PATTERN.matcher(f001).matches()) {
			return f001;
		}
		return null;
	}
}
