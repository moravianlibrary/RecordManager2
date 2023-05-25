package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.List;

public class CgsMetadataMarcRecord extends MetadataMarcRecord {

	public CgsMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') == null) {
				continue;
			}
			String comment = "";
			if (df.getSubfield('y') != null) {
				if (df.getSubfield('y').getData().equals("zdrojová publikace k objednání")) {
					comment = "zdrojová publikace k objednání z České geologické služby";
				} else {
					comment = df.getSubfield('y').getData();
				}
			}
			results.add(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
					Constants.DOCUMENT_AVAILABILITY_UNKNOWN, df.getSubfield('u').getData(), comment));
		}
		return results;
	}

}
