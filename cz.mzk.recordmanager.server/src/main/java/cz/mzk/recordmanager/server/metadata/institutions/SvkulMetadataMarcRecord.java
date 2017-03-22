package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.ArrayList;
import java.util.List;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SvkulMetadataMarcRecord extends MetadataMarcRecord {

	public SvkulMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public List<String> getBarcodes() {
		List<String> result = new ArrayList<>();
		super.getBarcodes().stream().forEach(bc -> result.add("31480" + bc));
		return result;
	}

	@Override
	public boolean matchFilter() {
		String f001;
		if ((f001 = underlayingMarc.getControlField("001")) != null && f001.startsWith("EZ")) {
			for (DataField df : underlayingMarc.getDataFields("910")) {
				if (df.getSubfield('b') != null) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
}
