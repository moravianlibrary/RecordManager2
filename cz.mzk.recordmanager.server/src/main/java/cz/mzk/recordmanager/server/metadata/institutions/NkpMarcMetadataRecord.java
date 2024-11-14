package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum.*;

public class NkpMarcMetadataRecord extends MetadataMarcRecord {

	public NkpMarcMetadataRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getClusterId() {
		return underlayingMarc.getControlField("001");
	}

	private List<HarvestedRecordFormatEnum> getMusicalScoresFormat() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());
		if (ldr06 == 'c') {
			return Collections.singletonList(MUSICAL_SCORES_PRINTED);
		} else if (ldr06 == 'd') {
			return Collections.singletonList(MUSICAL_SCORES_MANUSCRIPT);
		} else {
			return Collections.emptyList();
		}
	}

	private List<String> getNkpEversionsUrls(List<String> values, String availability) {
		List<String> results = new ArrayList<>();
		for (DataField field856 : underlayingMarc.getDataFields("856")) {
			String sfu = field856.getSubfield('u') != null ? field856.getSubfield('u').getData() : null;
			String sfy = field856.getSubfield('y') != null ? field856.getSubfield('y').getData() : null;
			if (sfu == null) continue;
			for (String value : values) {
				if (sfu.contains(value)) {
					results.add(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
							availability, sfu, sfy != null ? sfy : ""));
				}
			}
		}
		return results;
	}

	@Override
	public List<HarvestedRecordFormatEnum> getNkpRecordFormats() {
		List<HarvestedRecordFormatEnum> results = super.getDetectedFormatList();
		results.remove(MUSICAL_SCORES);
		results.addAll(getMusicalScoresFormat());
		return results;
	}

	@Override
	public List<String> getUrls() {
		List<String> results = super.getUrls();
		results.addAll(getNkpEversionsUrls(Arrays.asList("ebrary.com/", "proquest.com/"), Constants.DOCUMENT_AVAILABILITY_MEMBER));
		results.addAll(getNkpEversionsUrls(Arrays.asList("www.manuscriptorium.com/", "books.google.cz/"), Constants.DOCUMENT_AVAILABILITY_ONLINE));
		return results;
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		if (!getNkpEversionsUrls(Arrays.asList("ebrary.com/", "proquest.com/"), Constants.DOCUMENT_AVAILABILITY_MEMBER).isEmpty()) {
			return Collections.singletonList(EBOOK);
		}
		return super.getDetectedFormatList();
	}
}
