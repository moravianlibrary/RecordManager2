package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.marc4j.marc.DataField;

import java.util.*;

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

	@Override
	public List<HarvestedRecordFormatEnum> getAudioStreaming() {
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null && df.getSubfield('u').getData().contains("alexanderstreet.com/")) {
				for (DataField df990 : underlayingMarc.getDataFields("990")) {
					if (df990.getSubfield('a') == null) continue;
					if (df990.getSubfield('a').getData().contains("AM")) {
						return Collections.singletonList(AUDIO_STREAMING);
					}
				}
			}
			if (df.getSubfield('u') != null && df.getSubfield('u').getData().contains("naxosmusiclibrary.com/")) {
				return Collections.singletonList(AUDIO_STREAMING);
			}
		}
		return Collections.emptyList();
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
		List<HarvestedRecordFormatEnum> results = getDetectedFormatList();
		results.remove(MUSICAL_SCORES);
		results.addAll(getMusicalScoresFormat());
		return results;
	}

	private static final List<String> MEMBER_SOURCES = Arrays.asList(
			"ebrary.com/", "proquest.com/", "emerald.com/insight/", "emeraldinsight.com/",
			"naxosmusiclibrary.com/", "naxosmusiclibrary.com.ezproxy.nkp.cz/",
			"search.ebscohost.com/",
			"alexanderstreet.com/", "alexanderstreet.com.ezproxy.nkp.cz/"
	);
	private static final List<String> ONLINE_SOURCES = Arrays.asList("www.manuscriptorium.com/", "books.google.cz/");

	@Override
	public List<String> getUrls() {
		List<String> results = super.getUrls();

		results.addAll(getNkpEversionsUrls(MEMBER_SOURCES, Constants.DOCUMENT_AVAILABILITY_MEMBER));
		results.addAll(getNkpEversionsUrls(ONLINE_SOURCES, Constants.DOCUMENT_AVAILABILITY_ONLINE));
		return results;
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		if (!getNkpEversionsUrls(Arrays.asList("ebrary.com/", "proquest.com/"), Constants.DOCUMENT_AVAILABILITY_MEMBER).isEmpty()) {
			return Collections.singletonList(EBOOK);
		}
		return super.getDetectedFormatList();
	}

	@Override
	public Set<String> getAvailabilityForLocalOnlineFacet() {
		Set<String> result = new HashSet<>();
		for (String url : getUrls()) {
			try {
				if (Objects.equals(EVersionUrl.create(url).getAvailability(), Constants.DOCUMENT_AVAILABILITY_MEMBER)) {
					result.add(Constants.DOCUMENT_AVAILABILITY_MEMBER);
				}
			} catch (Exception ignore) {
			}
		}
		return result;
	}
}
