package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;

import java.util.ArrayList;
import java.util.List;

public class HistografbibMetadataMarcRecord extends MetadataMarcRecord {

	private static final String URL = "https://nacr.kpsys.cz/records/%s";

	public HistografbibMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>(super.getUrls());
		results.add(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
				Constants.DOCUMENT_AVAILABILITY_NA, String.format(URL, harvestedRecord.getUniqueId().getRecordId()), ""));
		return results;
	}

}
