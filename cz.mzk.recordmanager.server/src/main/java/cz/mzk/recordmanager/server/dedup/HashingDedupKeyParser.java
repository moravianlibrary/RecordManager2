package cz.mzk.recordmanager.server.dedup;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.mzk.recordmanager.server.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordFormatDAO;
import cz.mzk.recordmanager.server.util.MetadataUtils;

/**
 * Abstract DedupKeyParser implementation
 *
 * This implementation solves problem with repeated creation of deduplication keys
 *
 * @author mertam
 *
 */
public abstract class HashingDedupKeyParser implements DedupKeysParser {

	private final static int EFFECTIVE_TITLE_LENGTH = 255;
	private final static int EFFECTIVE_SOURCE_INFO_LENGTH = 255;
	private final static int EFFECTIVE_BL_CONSPECTUS_LENGTH = 255;
	private final static int EFFECTIVE_AUTHOR_LENGTH = 200;
	private final static int EFFECTIVE_AUTHOR_AUTH_KEY_LENGTH = 50;
	private final static int EFFECTIVE_LENGTH_30 = 30;
	private final static int EFFECTIVE_LENGTH_200 = 200;

	@Autowired
	private HarvestedRecordFormatDAO harvestedRecordFormatDAO;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Override
	public HarvestedRecord parse(HarvestedRecord record,
			MetadataRecord metadataRecord) throws DedupKeyParserException {

		record.setShouldBeProcessed(metadataRecord.matchFilter());
		record.setUpvApplicationId(metadataRecord.getUpvApplicationId()); // not dedup key
		record.setSigla(metadataRecord.getLibrarySigla()); // not dedup key
		if (!record.getHarvestedFrom().isGenerateDedupKeys()) {
			harvestedRecordDao.dropAuthorities(record);
			record.setAuthorities(metadataRecord.getAllAuthorAuthKey());
			return record;
		}
		boolean dedupKeysChanged = false;
		boolean oaiTimestampChanged = false;

		DedupKeysencapsulator encapsulator = new DedupKeysencapsulator();

		List<Title> titles = new ArrayList<>();
		for (Title title: metadataRecord.getTitle()) {
			title.setTitleStr(MetadataUtils.normalizeAndShorten(
						title.getTitleStr(),
						EFFECTIVE_TITLE_LENGTH));
			if (title.getTitleStr().isEmpty()) continue;
			if (!titles.contains(title)) {
				titles.add(title);
			}
		}
		encapsulator.setTitles(titles);
		List<ShortTitle> shortTitles = new ArrayList<>();
		for (ShortTitle shortTitle: metadataRecord.getShortTitles()) {
			shortTitle.setShortTitleStr(MetadataUtils.normalizeAndShorten(
					shortTitle.getShortTitleStr(), EFFECTIVE_TITLE_LENGTH));
			if (shortTitle.getShortTitleStr().isEmpty()) continue;
			if (!shortTitles.contains(shortTitle)) {
				shortTitles.add(shortTitle);
			}
		}
		encapsulator.setShortTitles(shortTitles);
		List<BLTitle> blTitles = new ArrayList<>();
		for (BLTitle blTitle : metadataRecord.getBLTitle()) {
			blTitle.setBLTitleStr(MetadataUtils.normalizeAndShorten(
					blTitle.getBLTitleStr(),
					EFFECTIVE_TITLE_LENGTH));
			if (blTitle.getBLTitleStr().isEmpty()) continue;
			if (!blTitles.contains(blTitle)) {
				blTitles.add(blTitle);
			}
		}
		encapsulator.setBlTitles(blTitles);
		List<BlCommonTitle> blCommonTitles = new ArrayList<>();
		for (BlCommonTitle blCommonTitle : metadataRecord.getBiblioLinkerCommonTitle()) {
			blCommonTitle.setBlCommonTitleStr(MetadataUtils.normalizeAndShorten(
					blCommonTitle.getBlCommonTitleStr(),
					EFFECTIVE_TITLE_LENGTH));
			if (blCommonTitle.getBlCommonTitleStr().isEmpty()) continue;
			if (!blCommonTitles.contains(blCommonTitle)) {
				blCommonTitles.add(blCommonTitle);
			}
		}
		encapsulator.setBlCommonTitle(blCommonTitles);

		encapsulator.setIsbns(metadataRecord.getISBNs());
		encapsulator.setIssns(metadataRecord.getISSNs());
		encapsulator.setIsmns(metadataRecord.getISMNs());
		encapsulator.setCnbs(metadataRecord.getCNBs());
		encapsulator.setPublicationYear(metadataRecord.getPublicationYear());
		List<HarvestedRecordFormatEnum> formatEnums = metadataRecord.getDetectedFormatList();
		encapsulator.setFormats(harvestedRecordFormatDAO.getFormatsFromEnums(formatEnums));
		encapsulator.setAuthorAuthKey(MetadataUtils.shorten(metadataRecord.getAuthorAuthKey(), EFFECTIVE_AUTHOR_AUTH_KEY_LENGTH));
		encapsulator.setAuthorString(MetadataUtils.normalizeAndShorten(metadataRecord.getAuthorString(), EFFECTIVE_AUTHOR_LENGTH));
		encapsulator.setScale(metadataRecord.getScale());
		encapsulator.setUuid(metadataRecord.getUUId());
		encapsulator.setPages(metadataRecord.getPageCount());
		encapsulator.setIssnSeries(MetadataUtils.normalize(metadataRecord.getISSNSeries()));
		encapsulator.setIssnSeriesOrder(MetadataUtils.normalize(metadataRecord.getISSNSeriesOrder()));
		encapsulator.setOclcs(metadataRecord.getOclcs());
		encapsulator.setClusterId(metadataRecord.getClusterId());
		encapsulator.setRaw001Id(metadataRecord.getRaw001Id());
		encapsulator.setSourceInfoT(MetadataUtils.normalizeAndShorten(metadataRecord.getSourceInfoT(), EFFECTIVE_SOURCE_INFO_LENGTH));
		encapsulator.setSourceInfoG(MetadataUtils.normalizeAndShorten(metadataRecord.getSourceInfoG(), EFFECTIVE_SOURCE_INFO_LENGTH));
		encapsulator.setSourceInfoX(MetadataUtils.normalizeAndShorten(metadataRecord.getSourceInfoX(), EFFECTIVE_LENGTH_30));
		encapsulator.setEans(metadataRecord.getEANs());
		encapsulator.setPublisherNumbers(metadataRecord.getPublisherNumber());
		encapsulator.setLanguages(new HashSet<>(metadataRecord.getLanguages()));
		encapsulator.setBlConspectus(MetadataUtils.normalizeAndShorten(metadataRecord.getConspectusForBiblioLinker(), EFFECTIVE_BL_CONSPECTUS_LENGTH));
		encapsulator.setBlAuthor(MetadataUtils.normalizeAndShorten(metadataRecord.getBiblioLinkerAuthor(), EFFECTIVE_AUTHOR_LENGTH));
		encapsulator.setBlAuthorAuthKey(MetadataUtils.shorten(metadataRecord.getBiblioLinkerAuthorAuth(), EFFECTIVE_AUTHOR_AUTH_KEY_LENGTH));
		encapsulator.setBlPublisher(MetadataUtils.normalizeAndShorten(metadataRecord.getBiblioLinkerPublisher(), EFFECTIVE_LENGTH_200));
		encapsulator.setBlSeries(MetadataUtils.normalizeAndShorten(metadataRecord.getBiblioLinkerSeries(), EFFECTIVE_LENGTH_200));
		encapsulator.setBlTopicKey(MetadataUtils.normalizeAndShorten(metadataRecord.getBiblioLinkerTopicKey(), EFFECTIVE_LENGTH_200));
		encapsulator.setBlEntities(metadataRecord.getBiblioLinkerEntity());
		encapsulator.setBlEntityAuthKeys(metadataRecord.getBiblioLinkerEntityAuthKey());
		encapsulator.setBlTitlePluses(metadataRecord.getBiblioLinkerTitlePlus());
		String computedHash = computeHashValue(encapsulator);
		String oldHash = record.getDedupKeysHash();
		String temporalHash = record.getTemporalDedupHash() == null ? "0000000000000000000000000000000000000000" : record.getTemporalDedupHash();

		// decide whether keys changed and should be updated in database
		// if temporal hash matches current hash, keys won't be updated
		// this prevents errors during processing one record multiple times
		// during one batch
		if ( (!temporalHash.equals(computedHash)) &&
				(oldHash == null || oldHash.isEmpty() || !computedHash.equals(oldHash))) {
			// keys changed, updated in database
			dedupKeysChanged = true;

			// drop old keys
			harvestedRecordDao.dropDedupKeys(record);
			if(record.getHarvestedFrom() != null) record.setWeight(metadataRecord.getWeight(record.getHarvestedFrom().getBaseWeight()));

			// assign new keys
			record.setTitles(encapsulator.getTitles());
			record.setIsbns(encapsulator.getIsbns());
			record.setIssns(encapsulator.getIssns());
			record.setIsmns(encapsulator.getIsmns());
			record.setCnb(encapsulator.getCnbs());
			record.setPublicationYear(encapsulator.getPublicationYear());
			record.setPhysicalFormats(harvestedRecordFormatDAO.getFormatsFromEnums(formatEnums));
			record.setAuthorAuthKey(encapsulator.getAuthorAuthKey());
			record.setAuthorString(encapsulator.getAuthorString());
			record.setScale(encapsulator.getScale());
			record.setUuid(encapsulator.getUuid());
			record.setPages(encapsulator.getPages());
			record.setIssnSeries(encapsulator.getIssnSeries());

			record.setIssnSeriesOrder(encapsulator.getIssnSeriesOrder());
			record.setOclcs(encapsulator.getOclcs());
			record.setLanguages(metadataRecord.getLanguages());
			record.setClusterId(encapsulator.getClusterId());
			record.setRaw001Id(encapsulator.getRaw001Id());
			record.setSourceInfoG(encapsulator.getSourceInfoG());
			record.setSourceInfoX(encapsulator.getSourceInfoX());
			record.setSourceInfoT(encapsulator.getSourceInfoT());
			record.setEans(encapsulator.getEans());
			record.setShortTitles(encapsulator.getShortTitles());
			record.setPublisherNumbers(metadataRecord.getPublisherNumber());
			record.setBlConspectus(encapsulator.getBlConspectus());
			record.setBlTitles(encapsulator.getBlTitles());
			record.setBlAuthor(encapsulator.getBlAuthor());
			record.setBlAuthorAuthKey(encapsulator.getBlAuthorAuthKey());
			record.setBlPublisher(encapsulator.getBlPublisher());
			record.setBlSeries(encapsulator.getBlSeries());
			record.setBlCommonTitle(encapsulator.getBlCommonTitle());
			record.setBlTopicKey(encapsulator.getBlTopicKey());
			record.setBlEntity(encapsulator.getBlEntities());
			record.setBlEntityAuthKey(encapsulator.getBlEntityAuthKeys());
			record.setBlTitlePluses(encapsulator.getBlTitlePluses());
			record.setTemporalDedupHash(computedHash);
		} else {
			harvestedRecordDao.dropAuthorities(record);
		}
		record.setAuthorities(metadataRecord.getAllAuthorAuthKey());
		record.setDedupKeysHash(computedHash);

		oaiTimestampChanged = record.getOaiTimestamp() != null && record.getTemporalOldOaiTimestamp() != null
				&& !record.getOaiTimestamp().equals(record.getTemporalOldOaiTimestamp());

		// decide whether record should be deduplicated
		if (dedupKeysChanged) {
			// new record or change in keys
			record.setNextDedupFlag(true);
			record.setNextBiblioLinkerFlag(true);
		} else {
			// key are equal
			if (oaiTimestampChanged) {
				// neither keys neither oai timestamp changed, 
				// don't deduplicate
				record.setNextDedupFlag(false);
				record.setNextBiblioLinkerFlag(false);
			} else {
				// keys are same but timestamp changed
				// keep previsous dedup flag
				// this may happen during repeated harvesting before 
			}
		}
		return record;
	}

	/**
	 * Compute SHA1 hash of deduplication keys from given {@link DedupKeysencapsulator}
	 * @param encapsulator {@link DedupKeysencapsulator}
	 * @return Hash as String
	 */
	protected String computeHashValue(final DedupKeysencapsulator encapsulator) {

		try {
				// change of hash function also requires changes in database row
				MessageDigest md = MessageDigest.getInstance("SHA-1");

			for (Title t : encapsulator.getTitles()) {
					md.update(t.getTitleStr().getBytes("utf-8"));
				}

			for (Isbn i : encapsulator.getIsbns()) {
					md.update(i.getIsbn().byteValue());
				}

			for (Issn i : encapsulator.getIssns()) {
					md.update(i.getIssn().getBytes());
				}

			for (Ismn i : encapsulator.getIsmns()) {
					md.update(i.getIsmn().byteValue());
				}

			for (Cnb c : encapsulator.getCnbs()) {
					md.update(c.getCnb().getBytes());
				}

			if (encapsulator.getPublicationYear() != null) {
					md.update(encapsulator.getPublicationYear().byteValue());
				}

			for (HarvestedRecordFormat hrfe : encapsulator.getFormats()) {
					md.update(hrfe.getName().getBytes());
				}

			if (encapsulator.getAuthorAuthKey() != null) {
					md.update(encapsulator.getAuthorAuthKey().getBytes());
			}

				if (encapsulator.getAuthorString() != null) {
					md.update(encapsulator.getAuthorString().getBytes());
				}

			if (encapsulator.getScale() != null) {
					md.update(encapsulator.getScale().byteValue());
				}

			if (encapsulator.getUuid() != null) {
					md.update(encapsulator.getUuid().getBytes());
				}

			if (encapsulator.getPages() != null) {
					md.update(encapsulator.getPages().byteValue());
				}

			if (encapsulator.getIssnSeries() != null) {
					md.update(encapsulator.getIssnSeries().getBytes());
				}

			if (encapsulator.getIssnSeriesOrder() != null) {
					md.update(encapsulator.getIssnSeriesOrder().getBytes());
				}

			for (Oclc o : encapsulator.getOclcs()) {
					md.update(o.getOclcStr().getBytes());
				}

			for (String l : encapsulator.getLanguages()) {
					md.update(l.getBytes());
				}

			if (encapsulator.getClusterId() != null) {
					md.update(encapsulator.getClusterId().getBytes());
				}

			if (encapsulator.getRaw001Id() != null) {
					md.update(encapsulator.getRaw001Id().getBytes());
				}

				if (encapsulator.getSourceInfoT() != null) {
					md.update(encapsulator.getSourceInfoT().getBytes());
				}

				if (encapsulator.getSourceInfoX() != null) {
					md.update(encapsulator.getSourceInfoX().getBytes());
				}

				if (encapsulator.getSourceInfoG() != null) {
					md.update(encapsulator.getSourceInfoG().getBytes());
				}

			if (encapsulator.getBlAuthor() != null) {
				md.update(encapsulator.getBlAuthor().getBytes());
			}

			if (encapsulator.getBlAuthorAuthKey() != null) {
				md.update(encapsulator.getBlAuthorAuthKey().getBytes());
			}

			if (encapsulator.getBlPublisher() != null) {
				md.update(encapsulator.getBlPublisher().getBytes());
			}

			if (encapsulator.getBlSeries() != null) {
				md.update(encapsulator.getBlSeries().getBytes());
			}

			if (encapsulator.getBlTopicKey() != null) {
				md.update(encapsulator.getBlTopicKey().getBytes());
			}

			for (Ean ean: encapsulator.getEans()) {
					md.update(ean.getEan().byteValue());
				}

			for (PublisherNumber publisherNumber : encapsulator.getPublisherNumbers()) {
					md.update(publisherNumber.getPublisherNumber().getBytes("utf-8"));
				}

			for (ShortTitle st : encapsulator.getShortTitles()) {
					md.update(st.getShortTitleStr().getBytes("utf-8"));
				}

			for (BLTitle blTitle : encapsulator.getBlTitles()) {
				md.update(blTitle.getBLTitleStr().getBytes("utf-8"));
			}

			for (BlCommonTitle blCommonTitle : encapsulator.getBlCommonTitle()) {
				md.update(blCommonTitle.getBlCommonTitleStr().getBytes("utf-8"));
			}

			for (BLEntity blEntity : encapsulator.getBlEntities()) {
				md.update(blEntity.getBLEntityStr().getBytes("utf-8"));
			}

			for (BLEntityAuthKey blEntityAuthKey : encapsulator.getBlEntityAuthKeys()) {
				md.update(blEntityAuthKey.getBLEntityAuthKeyStr().getBytes("utf-8"));
			}

			for (BLTitlePlus blTitlePlus : encapsulator.getBlTitlePluses()) {
				md.update(blTitlePlus.getBLTitlePlusStr().getBytes("utf-8"));
			}

				if (encapsulator.getBlConspectus() != null) {
					md.update(encapsulator.getBlConspectus().getBytes());
				}

			byte[] hash = md.digest();
				StringBuilder sb = new StringBuilder();
			    for (byte b : hash) {
			        sb.append(String.format("%02x", b));
			    }

			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
				// should never be thrown, SHA-1 is required by Java specification
		}
			catch (UnsupportedEncodingException uee) {
				throw new DedupKeyParserException("Uncoding problems in hash computation", uee);
			}

		return "";

		}

	/**
		 * compute SHA-1 hash of deduplication keys for given {@link HarvestedRecord}
		 * @param hr {@link HarvestedRecord}
		 * @return Hash as String
		 */
		protected String computeHashValue(final HarvestedRecord hr) {
			DedupKeysencapsulator encapsulator = new DedupKeysencapsulator();

			encapsulator.setTitles(hr.getTitles());
			encapsulator.setIsbns(hr.getIsbns());
			encapsulator.setIssns(hr.getIssns());
			encapsulator.setIsmns(hr.getIsmns());
			encapsulator.setCnbs(hr.getCnb());
			encapsulator.setPublicationYear(hr.getPublicationYear());
			encapsulator.setFormats(hr.getPhysicalFormats());
			encapsulator.setAuthorAuthKey(hr.getAuthorAuthKey());
			encapsulator.setAuthorString(hr.getAuthorString());
			encapsulator.setScale(hr.getScale());
			encapsulator.setUuid(hr.getUuid());
			encapsulator.setPages(hr.getPages());
			encapsulator.setIssnSeries(hr.getIssnSeries());
			encapsulator.setIssnSeriesOrder(hr.getIssnSeriesOrder());
			encapsulator.setOclcs(hr.getOclcs());
			encapsulator.setClusterId(hr.getClusterId());
			encapsulator.setRaw001Id(hr.getRaw001Id());
			encapsulator.setEans(hr.getEans());
			encapsulator.setShortTitles(hr.getShortTitles());
			encapsulator.setPublisherNumbers(hr.getPublisherNumbers());
			encapsulator.setSourceInfoG(hr.getSourceInfoG());
			encapsulator.setSourceInfoX(hr.getSourceInfoX());
			encapsulator.setSourceInfoT(hr.getSourceInfoT());
			encapsulator.setBlConspectus(hr.getBlConspectus());
			encapsulator.setBlTitles(hr.getBlTitles());
			encapsulator.setBlAuthor(hr.getBlAuthor());
			encapsulator.setBlAuthorAuthKey(hr.getBlAuthorAuthKey());
			encapsulator.setBlPublisher(hr.getBlPublisher());
			encapsulator.setBlSeries(hr.getBlSeries());
			encapsulator.setBlTopicKey(hr.getBlTopicKey());
			encapsulator.setBlCommonTitle(hr.getBlCommonTitle());
			encapsulator.setBlEntities(hr.getBlEntity());
			encapsulator.setBlEntityAuthKeys(hr.getBlEntityAuthKey());
			encapsulator.setBlTitlePluses(hr.getBlTitlePluses());
			return computeHashValue(encapsulator);
		}

	protected class DedupKeysencapsulator {
		List<Title> titles = new ArrayList<>();
		List<Isbn> isbns = new ArrayList<>();
		List<Issn> issns = new ArrayList<>();
		List<Ismn> ismns = new ArrayList<>();
		List<Cnb> cnbs = new ArrayList<>();
		List<Oclc> oclcs = new ArrayList<>();
		List<HarvestedRecordFormat> formats = new ArrayList<>();
		Set<String> languages = new HashSet<>();
		List<Ean> eans = new ArrayList<>();
		List<ShortTitle> shortTitles = new ArrayList<>();
		List<PublisherNumber> publisherNumbers = new ArrayList<>();
		List<BLTitle> blTitles = new ArrayList<>();
		List<BlCommonTitle> blCommonTitle = new ArrayList<>();
		List<BLEntity> blEntities = new ArrayList<>();
		List<BLEntityAuthKey> blEntityAuthKeys = new ArrayList<>();
		List<BLTitlePlus> blTitlePluses = new ArrayList<>();

		Long publicationYear;
		String authorString;
		String authorAuthKey;
		Long scale;
		String uuid;
		Long pages;
		String issnSeries;
		String issnSeriesOrder;
		String clusterId;
		String raw001Id;
		String sourceInfoX;
		String sourceInfoT;
		String sourceInfoG;
		String blConspectus;
		String blAuthor;
		String blAuthorAuthKey;
		String blPublisher;
		String blSeries;
		String blTopicKey;

		public List<Ismn> getIsmns() {
			return ismns;
		}
		public void setIsmns(List<Ismn> ismns) {
			this.ismns = ismns;
		}
		public List<Title> getTitles() {
			return titles != null ? titles : Collections.emptyList();
		}
		public void setTitles(List<Title> titles) {
			this.titles = titles;
		}
		public List<Isbn> getIsbns() {
			return isbns != null ? isbns : Collections.emptyList();
		}
		public void setIsbns(List<Isbn> isbns) {
			this.isbns = isbns;
		}
		public List<Issn> getIssns() {
			return issns != null ? issns : Collections.emptyList();
		}
		public void setIssns(List<Issn> issns) {
			this.issns = issns;
		}
		public List<Cnb> getCnbs() {
			return cnbs != null ? cnbs : Collections.emptyList();
		}
		public void setCnbs(List<Cnb> cnbs) {
			this.cnbs = cnbs;
		}
		public List<Oclc> getOclcs() {
			return oclcs != null ? oclcs : Collections.emptyList();
		}
		public void setOclcs(List<Oclc> oclcs) {
			this.oclcs = oclcs;
		}
		public List<HarvestedRecordFormat> getFormats() {
			return formats != null ? formats : Collections.emptyList();
		}
		public void setFormats(List<HarvestedRecordFormat> formats) {
			this.formats = formats;
		}
		public Set<String> getLanguages() {
			return languages;
		}
		public void setLanguages(Set<String> languages) {
			this.languages = languages != null ? languages : Collections.emptySet();
		}
		public Long getPublicationYear() {
			return publicationYear;
		}
		public void setPublicationYear(Long publicationYear) {
			this.publicationYear = publicationYear;
		}
		public String getAuthorString() {
			return authorString;
		}
		public void setAuthorString(String authorString) {
			this.authorString = authorString;
		}
		public String getAuthorAuthKey() {
			return authorAuthKey;
		}
		public void setAuthorAuthKey(String authorAuthKey) {
			this.authorAuthKey = authorAuthKey;
		}
		public Long getScale() {
			return scale;
		}
		public void setScale(Long scale) {
			this.scale = scale;
		}
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public Long getPages() {
			return pages;
		}
		public void setPages(Long pages) {
			this.pages = pages;
		}
		public String getIssnSeries() {
			return issnSeries;
		}
		public void setIssnSeries(String issnSeries) {
			this.issnSeries = issnSeries;
		}
		public String getIssnSeriesOrder() {
			return issnSeriesOrder;
		}
		public void setIssnSeriesOrder(String issnSeriesOrder) {
			this.issnSeriesOrder = issnSeriesOrder;
		}
		public String getClusterId() {
			return clusterId;
		}
		public void setClusterId(String clusterId) {
			this.clusterId = clusterId;
		}
		public String getRaw001Id() {
			return raw001Id;
		}
		public void setRaw001Id(String raw001Id) {
			this.raw001Id = raw001Id;
		}
		public List<Ean> getEans() {
			return eans;
		}
		public void setEans(List<Ean> eans) {
			this.eans = eans;
		}
		public List<ShortTitle> getShortTitles() {
			return shortTitles;
		}
		public void setShortTitles(List<ShortTitle> shortTitles) {
			this.shortTitles = shortTitles;
		}
		public List<PublisherNumber> getPublisherNumbers() {
			return publisherNumbers;
		}
		public void setPublisherNumbers(List<PublisherNumber> publisherNumbers) {
			this.publisherNumbers = publisherNumbers;
		}

		public String getSourceInfoX() {
			return sourceInfoX;
		}

		public void setSourceInfoX(String sourceInfoX) {
			this.sourceInfoX = sourceInfoX;
		}

		public String getSourceInfoT() {
			return sourceInfoT;
		}

		public void setSourceInfoT(String sourceInfoT) {
			this.sourceInfoT = sourceInfoT;
		}

		public String getSourceInfoG() {
			return sourceInfoG;
		}

		public void setSourceInfoG(String sourceInfoG) {
			this.sourceInfoG = sourceInfoG;
		}

		public String getBlConspectus() {
			return blConspectus;
		}

		public void setBlConspectus(String blConspectus) {
			this.blConspectus = blConspectus;
		}

		public String getBlAuthor() {
			return blAuthor;
		}

		public void setBlAuthor(String blAuthor) {
			this.blAuthor = blAuthor;
		}

		public String getBlAuthorAuthKey() {
			return blAuthorAuthKey;
		}

		public void setBlAuthorAuthKey(String blAuthorAuthKey) {
			this.blAuthorAuthKey = blAuthorAuthKey;
		}

		public String getBlPublisher() {
			return blPublisher;
		}

		public void setBlPublisher(String blPublisher) {
			this.blPublisher = blPublisher;
		}

		public List<BLTitle> getBlTitles() {
			return blTitles;
		}

		public void setBlTitles(List<BLTitle> blTitles) {
			this.blTitles = blTitles;
		}

		public String getBlSeries() {
			return blSeries;
		}

		public void setBlSeries(String blSeries) {
			this.blSeries = blSeries;
		}

		public List<BlCommonTitle> getBlCommonTitle() {
			return blCommonTitle;
		}

		public void setBlCommonTitle(List<BlCommonTitle> blCommonTitle) {
			this.blCommonTitle = blCommonTitle;
		}

		public String getBlTopicKey() {
			return blTopicKey;
		}

		public void setBlTopicKey(String blTopicKey) {
			this.blTopicKey = blTopicKey;
		}

		public List<BLEntity> getBlEntities() {
			return blEntities;
		}

		public void setBlEntities(List<BLEntity> blEntities) {
			this.blEntities = blEntities;
		}

		public List<BLEntityAuthKey> getBlEntityAuthKeys() {
			return blEntityAuthKeys;
		}

		public void setBlEntityAuthKeys(List<BLEntityAuthKey> blEntityAuthKeys) {
			this.blEntityAuthKeys = blEntityAuthKeys;
		}

		public List<BLTitlePlus> getBlTitlePluses() {
			return blTitlePluses;
		}

		public void setBlTitlePluses(List<BLTitlePlus> blTitlePluses) {
			this.blTitlePluses = blTitlePluses;
		}
	}

}
