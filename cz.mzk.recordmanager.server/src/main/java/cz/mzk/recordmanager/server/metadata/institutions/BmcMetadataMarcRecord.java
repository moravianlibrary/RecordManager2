package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.List;

public class BmcMetadataMarcRecord extends MetadataMarcRecord {

	private static final String SUBJECT_FACET_FILE = "subject_facet_bmc.txt";
	private static final String LINK_MEDVIK = "https://www.medvik.cz/bmc/link.do?id=%s";
	private static final String LINK_DOI = "https://dx.doi.org/%s";
	private static final String COMMENT_MEDVIK = "záznam v BMČ";
	private static final String COMMENT_DOI = "DOI";

	public BmcMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>(super.getUrls());

		if (underlayingMarc.getControlField("001") != null) {
			results.add(EVersionUrl.create(harvestedRecord.getHarvestedFrom().getIdPrefix(), Constants.DOCUMENT_AVAILABILITY_UNKNOWN,
					String.format(LINK_MEDVIK, underlayingMarc.getControlField("001")), COMMENT_MEDVIK).toString());
		}
		for (DataField df : underlayingMarc.getDataFields("024")) {
			if (df.getSubfield('2') != null && df.getSubfield('2').getData().equalsIgnoreCase("doi")
					&& df.getSubfield('a') != null) {
				results.add(EVersionUrl.create(harvestedRecord.getHarvestedFrom().getIdPrefix(), Constants.DOCUMENT_AVAILABILITY_UNKNOWN,
						String.format(LINK_DOI, df.getSubfield('a').getData()), COMMENT_DOI).toString());
			}
		}
		return results;
	}

	@Override
	public String filterSubjectFacet() {
		return SUBJECT_FACET_FILE;
	}
}
