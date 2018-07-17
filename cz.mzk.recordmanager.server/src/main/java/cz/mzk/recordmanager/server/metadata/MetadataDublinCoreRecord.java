package cz.mzk.recordmanager.server.metadata;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.model.*;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import cz.mzk.recordmanager.server.util.identifier.ISMNUtils;
import cz.mzk.recordmanager.server.util.identifier.ISSNUtils;
import cz.mzk.recordmanager.server.util.identifier.NoDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MetadataDublinCoreRecord implements MetadataRecord {

	private static Logger logger = LoggerFactory.getLogger(MetadataDublinCoreRecord.class);

	protected DublinCoreRecord dcRecord;
	protected HarvestedRecord harvestedRecord = null;

	private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	private static final Pattern DC_UUID_PATTERN = Pattern.compile("^uuid:(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DC_ISBN_PATTERN = Pattern
			.compile("isbn:\\s*([\\dxX-]*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DC_ISSN_PATTERN = Pattern
			.compile("issn:(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DC_CNB_PATTERN = Pattern
			.compile("^ccnb:(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DC_OCLC_PATTERN = Pattern
			.compile("^oclc:(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DC_ISMN_PATTERN = Pattern
			.compile("^ismn:([\\dM\\s\\-]*)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DC_IDENTIFIER_PATTERN = Pattern
			.compile(".*:.*", Pattern.CASE_INSENSITIVE);
	private static final Pattern DC_TYPE_KRAMERIUS_PATTERN = Pattern.compile("^model:(.*)");

	public MetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		initRecords(dcRecord, null);
	}

	public MetadataDublinCoreRecord(DublinCoreRecord dcRecord, HarvestedRecord hr) {
		initRecords(dcRecord, hr);
	}

	private void initRecords(DublinCoreRecord dcRecord, HarvestedRecord hr) {
		if (dcRecord == null) {
			throw new IllegalArgumentException(
					"Creating MetadataDublinCoreRecord with NULL underlying dcRecord.");
		}
		this.dcRecord = dcRecord;
		this.harvestedRecord = hr;
	}

	@Override
	public List<Title> getTitle() {
		List<Title> result = new ArrayList<>();
		Long titleOrder = 0L;

		for (String s : dcRecord.getTitles()) {
			result.add(Title.create(s, ++titleOrder, MetadataUtils.similarityEnabled(s)));
		}

		return result;
	}

	@Override
	public Long getPublicationYear() {
		// expecting year in date (should work for Kramerius)
		String year = dcRecord.getFirstDate();
		if (year == null) return null;
		Matcher matcher = YEAR_PATTERN.matcher(year);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException ignore) {
		}
		return null;
	}

	@Override
	public String export(IOFormat iOFormat) {
		return dcRecord.export(iOFormat);
	}

	@Override
	public String getUniqueId() {
		// expecting unique id in first identifier
		return this.dcRecord.getFirstIdentifier();
	}

	/**
	 * go through all identifiers, look for isbn:.*, validate isbn
	 *
	 * @return list of {@link Isbn}
	 */
	@Override
	public List<Isbn> getISBNs() {
		List<Isbn> isbns = new ArrayList<>();
		Matcher matcher;
		Long isbnCounter = 0L;

		for (String identifier : dcRecord.getIdentifiers()) {
			String rawIsbnStr = "";
			if ((matcher = DC_ISBN_PATTERN.matcher(identifier)).find()) rawIsbnStr = matcher.group(1);
			else if (!DC_IDENTIFIER_PATTERN.matcher(identifier).matches()) rawIsbnStr = identifier;
			Long isbnLong;
			try {
				isbnLong = ISBNUtils.toISBN13LongThrowing(rawIsbnStr);
			} catch (NoDataException nde) {
				continue;
			} catch (NumberFormatException nfe) {
				logger.info(String.format("Invalid ISBN: %s", rawIsbnStr));
				continue;
			}
			isbns.add(Isbn.create(isbnLong, ++isbnCounter, ""));
		}
		return isbns;
	}

	@Override
	public List<Issn> getISSNs() {
		List<Issn> results = new ArrayList<>();
		Long issnCounter = 0L;
		Matcher matcher;

		for (String identifier : dcRecord.getIdentifiers()) {
			String rawIssnStr = "";
			if ((matcher = DC_ISSN_PATTERN.matcher(identifier)).find()) rawIssnStr = matcher.group(1);
			else if (!DC_IDENTIFIER_PATTERN.matcher(identifier).matches()) rawIssnStr = identifier;
			String validIssn;
			try {
				validIssn = ISSNUtils.getValidIssnThrowing(rawIssnStr);
			} catch (NoDataException nde) {
				continue;
			} catch (NumberFormatException nfe) {
				logger.info(String.format("Invalid ISSN: %s", rawIssnStr));
				continue;
			}
			results.add(Issn.create(validIssn, ++issnCounter, ""));
		}
		return results;
	}

	@Override
	public Long getPageCount() {
		// getting page count from dc:format via regexp is not accurate enough ... leaving with null
		return null;
	}

	// note: recognized Kramerius formats are in c.m.r.server.kramerius.FedoraModels;
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> hrf = new ArrayList<>();

		if (isBook()) hrf.add(HarvestedRecordFormatEnum.BOOKS);
		if (isPeriodical()) hrf.add(HarvestedRecordFormatEnum.PERIODICALS);
		if (isMap()) hrf.add(HarvestedRecordFormatEnum.MAPS);
		if (isVisualDocument()) hrf.add(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS);
		if (isMusicalScores()) hrf.add(HarvestedRecordFormatEnum.MUSICAL_SCORES);
		if (isAudioDocument()) hrf.add(HarvestedRecordFormatEnum.AUDIO_OTHER);
		if (isOtherDocument()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		return hrf;
	}

	protected boolean isBook() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:monograph") || f.equals("Digitized Monograph")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isPeriodical() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:periodical") || f.equals("Digitized Periodical")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isMap() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:map")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isVisualDocument() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:graphic")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isMusicalScores() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:sheetmusic")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isAudioDocument() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:soundrecording")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isOtherDocument() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:archive") || f.equals("model:manuscript")) {
				return true;
			}
		}
		return false;
	}

	/* there may be more than one rights value in Kramerius, but only one "policy:.*" */
	public String getPolicyKramerius() {
		List<String> rights = dcRecord.getRights();
		String policy = "unknown";
		for (String f : rights) {
			if (f.equals("policy:public")) {
				return "public";
			} else if (f.equals("policy:private")) {
				return "private";
			}
		}
		return policy;
	}

	public String getModelKramerius() {
		List<String> types = dcRecord.getTypes();
		Matcher m;
		for (String f : types) {
			m = DC_TYPE_KRAMERIUS_PATTERN.matcher(f);
			if (m.find()) {
				return m.group(1).trim();
			}
		}
		return "unknown";
	}

	@Override
	public List<Cnb> getCNBs() {
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Cnb> cnbs = new ArrayList<>();
		Matcher matcher;

		for (String f : identifiers) {
			matcher = DC_CNB_PATTERN.matcher(f);
			if (matcher.find()) {
				cnbs.add(Cnb.create(matcher.group(1).trim()));
			}
		}
		return cnbs;
	}

	@Override
	public String getISSNSeries() {
		// difficult do identify.. leaving with null
		return null;
	}

	@Override
	public String getISSNSeriesOrder() {
		// no way how to get.. leaving with null
		return null;
	}

	@Override
	public Long getWeight(Long baseWeight) {
		// leaving with null
		return null;
	}

	@Override
	public Long getScale() {
		// leaving with null
		return null;
	}

	@Override
	public String getUUId() {
		List<String> identifiers = dcRecord.getIdentifiers();
		String uuid = "";
		Matcher m;

		for (String f : identifiers) {
			m = DC_UUID_PATTERN.matcher(f);
			if (m.find()) {
				uuid = m.group(1).trim();
			}
		}
		if (uuid.isEmpty()) return null;
		else return uuid;
	}

	@Override
	public String getAuthorAuthKey() {
		// can't get authority key from DC .. leaving with null
		return null;
	}

	@Override
	public String getAuthorString() {
		return dcRecord.getFirstCreator();
	}

	@Override
	public String getClusterId() {
		// Nothing to return
		return null;
	}

	@Override
	public List<Oclc> getOclcs() {
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Oclc> oclcs = new ArrayList<>();
		if (getLanguages().contains("cze")) return oclcs;
		Matcher matcher;

		for (String f : identifiers) {
			matcher = DC_OCLC_PATTERN.matcher(f);
			if (matcher.find()) {
				Oclc oclc = new Oclc();
				oclc.setOclcStr(matcher.group(1).trim());
				oclcs.add(oclc);
			}
		}
		return oclcs;
	}

	@Override
	public List<String> getLanguages() {
		return dcRecord.getLanguages().stream().filter(lang -> lang.equals("eng") || lang.equals("cze")).collect(Collectors.toList());
	}

	@Override
	public boolean matchFilter() {
		boolean isTitle = false;
		for (Title title : getTitle()) {
			if (!title.getTitleStr().isEmpty()) {
				isTitle = true;
				break;
			}
		}
		return isTitle;
	}

	@Override
	public String getRaw001Id() {
		// Nothing to return
		return null;
	}

	@Override
	public CitationRecordType getCitationFormat() {
		// Nothing to return
		return null;
	}

	@Override
	public List<Ismn> getISMNs() {
		List<Ismn> results = new ArrayList<>();
		Long ismnCounter = 0L;
		Matcher matcher;

		for (String identifier : dcRecord.getIdentifiers()) {
			String rawIsmnStr = "";
			if ((matcher = DC_ISMN_PATTERN.matcher(identifier)).find()) rawIsmnStr = matcher.group(1);
			else if (!DC_IDENTIFIER_PATTERN.matcher(identifier).matches()) rawIsmnStr = identifier;
			Long validIsmnLong;
			try {
				validIsmnLong = ISMNUtils.toIsmn13LongThrowing(rawIsmnStr);
			} catch (NoDataException nde) {
				continue;
			} catch (NumberFormatException nfe) {
				logger.info(String.format("Invalid ISMN: %s", rawIsmnStr));
				continue;
			}
			results.add(Ismn.create(validIsmnLong, ++ismnCounter, ""));
		}
		return results;
	}

	@Override
	public String getAuthorityId() {
		// can't get authority key from DC .. leaving with null
		return null;
	}

	@Override
	public List<String> getUrls() {
		return dcRecord.getUrls();
	}

	@Override
	public String filterSubjectFacet() {
		// implemented only in institution specific classes
		return null;
	}

	@Override
	public List<Ean> getEANs() {
		// Nothing to return
		return Collections.emptyList();
	}

	@Override
	public List<ShortTitle> getShortTitles() {
		// Nothing to return
		return Collections.emptyList();
	}

	@Override
	public List<String> getDefaultStatuses() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public List<String> getInternationalPatentClassfication() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public TezaurusKey getTezaurusKey() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public Boolean getMetaproxyBool() {
		// Nothing to return
		return null;
	}

	@Override
	public List<PublisherNumber> getPublisherNumber() {
		return Collections.emptyList();
	}

	@Override
	public String getSourceInfoX() {
		// Nothing to return
		return null;
	}

	@Override
	public String getSourceInfoT() {
		// Nothing to return
		return null;
	}

	@Override
	public String getSourceInfoG() {
		// Nothing to return
		return null;
	}

}
