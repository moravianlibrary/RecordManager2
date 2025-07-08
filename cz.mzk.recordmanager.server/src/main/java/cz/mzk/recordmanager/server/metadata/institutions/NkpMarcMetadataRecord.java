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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum.*;

public class NkpMarcMetadataRecord extends MetadataMarcRecord {

	public NkpMarcMetadataRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d+)");

	protected enum NkpSource {
		NKP {
			@Override
			public boolean check(HarvestedRecord hr) {
				return hr.getUniqueId().getHarvestedFromId() == Constants.IMPORT_CONF_NKP;
			}
		},
		SLK {
			@Override
			public boolean check(HarvestedRecord hr) {
				return hr.getUniqueId().getHarvestedFromId() == Constants.IMPORT_CONF_ID_SLK;
			}
		},
		STT {
			@Override
			public boolean check(HarvestedRecord hr) {
				return hr.getUniqueId().getHarvestedFromId() == Constants.IMPORT_CONF_ID_STT;
			}
		},
		KKL {
			@Override
			public boolean check(HarvestedRecord hr) {
				return hr.getUniqueId().getHarvestedFromId() == Constants.IMPORT_CONF_ID_KKL;
			}
		},
		NONE {
			@Override
			public boolean check(HarvestedRecord hr) {
				return false;
			}
		};

		public abstract boolean check(HarvestedRecord hr);

		public static NkpSource getSource(HarvestedRecord hr) {

			for (NkpSource source : NkpSource.values()) {
				if (source.check(hr)) return source;
			}
			return NONE;
		}
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

	@Override
	public boolean isEod() {
		if (NkpSource.getSource(harvestedRecord) == NkpSource.KKL) {
			return false;
		}
		for (String url : underlayingMarc.getFields("856", 'u')) {
			if (url.contains("uuid")) return false;
			for (String onlineSource : ONLINE_SOURCES) {
				if (url.contains(onlineSource)) return false;
			}
		}
		Long publicationYear = getPublicationYear();
		if (Collections.disjoint(underlayingMarc.getFields("990", 'a'), Collections.singletonList("BK")))
			return false;

		for (String value : underlayingMarc.getFields("994", 'a')) {
			if (value.equals("Y")) return true;
			if (value.startsWith("Volné dílo od r")) {
				Matcher matcher = YEAR_PATTERN.matcher(value);
				if (matcher.find()) {
					int year = Integer.parseInt(matcher.group(1));
					return year <= Calendar.getInstance().get(Calendar.YEAR);
				}
			}
		}
		if (publicationYear != null && publicationYear <= (Calendar.getInstance().get(Calendar.YEAR) - 100)) {
			return true;
		}

		return false;
	}
}
