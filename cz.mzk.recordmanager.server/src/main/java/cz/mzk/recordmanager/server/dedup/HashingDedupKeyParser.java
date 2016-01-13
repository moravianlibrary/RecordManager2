package cz.mzk.recordmanager.server.dedup;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.model.Title;
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
	
	@Autowired 
	private HarvestedRecordFormatDAO harvestedRecordFormatDAO;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Override
	public HarvestedRecord parse(HarvestedRecord record,
			MetadataRecord metadataRecord) throws DedupKeyParserException {

		boolean dedupKeysChanged = false;
		boolean oaiTimestampChanged = false;
		
		DedupKeysencapsulator encapsulator = new DedupKeysencapsulator();
		
		List<Title> titles = new ArrayList<>();
		for (Title title: metadataRecord.getTitle()) {
			title.setTitleStr(MetadataUtils.normalizeAndShorten(
						title.getTitleStr(),
						EFFECTIVE_TITLE_LENGTH));
			if (!titles.contains(title)) {
				titles.add(title);
			}
		}
		encapsulator.setTitles(titles);
		encapsulator.setIsbns(metadataRecord.getISBNs());
		encapsulator.setIssns(metadataRecord.getISSNs());
		encapsulator.setCnbs(metadataRecord.getCNBs());
		encapsulator.setPublicationYear(metadataRecord.getPublicationYear());
		List<HarvestedRecordFormatEnum> formatEnums = metadataRecord.getDetectedFormatList();
		encapsulator.setFormats(harvestedRecordFormatDAO.getFormatsFromEnums(formatEnums));
		encapsulator.setAuthorAuthKey(metadataRecord.getAuthorAuthKey());
		encapsulator.setAuthorString(MetadataUtils.normalize(metadataRecord.getAuthorString()));
		encapsulator.setScale(metadataRecord.getScale());
		encapsulator.setUuid(metadataRecord.getUUId());
		encapsulator.setPages(metadataRecord.getPageCount());
		encapsulator.setIssnSeries(MetadataUtils.normalize(metadataRecord.getISSNSeries()));
		encapsulator.setIssnSeriesOrder(MetadataUtils.normalize(metadataRecord.getISSNSeriesOrder()));
		encapsulator.setOclcs(metadataRecord.getOclcs());
		encapsulator.setClusterId(metadataRecord.getClusterId());
		encapsulator.setRaw001Id(metadataRecord.getRaw001Id());
		
		encapsulator.setLanguages(new HashSet<>(metadataRecord.getLanguages()));
		
		

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
			
			if(record.getHarvestedFrom() != null) record.setWeight(metadataRecord.getWeight(record.getHarvestedFrom().getBaseWeight()));
			// drop old keys
			harvestedRecordDao.dropDedupKeys(record);
			if(record.getHarvestedFrom() != null) record.setWeight(metadataRecord.getWeight(record.getHarvestedFrom().getBaseWeight()));
			
			// assign new keys
			record.setTitles(encapsulator.getTitles());
			record.setIsbns(encapsulator.getIsbns());
			record.setIssns(encapsulator.getIssns());
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
			record.setShouldBeProcessed(metadataRecord.matchFilter());
			record.setRaw001Id(encapsulator.getRaw001Id());
			
			record.setTemporalDedupHash(computedHash);
		} 
		
		record.setDedupKeysHash(computedHash);
		
		
		if (record.getOaiTimestamp() != null && record.getTemporalOldOaiTimestamp() != null
				&& !record.getOaiTimestamp().equals(record.getTemporalOldOaiTimestamp())) {
			oaiTimestampChanged = true;
		} else {
			oaiTimestampChanged = false;
		}
		
		
		// decide whether record should be deduplicated
		if (dedupKeysChanged) {
			// new record or change in keys
			record.setNextDedupFlag(true);
		} else {
			// key are equal
			if (oaiTimestampChanged) {
				// neither keys neither oai timestamp changed, 
				// don't deduplicate
				record.setNextDedupFlag(false);
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
	 * @param encapsulator
	 * @return
	 */
	protected String computeHashValue(final DedupKeysencapsulator encapsulator) {
	
			try {
				// change of hash function also requires changes in database row
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				
				for (Title t: encapsulator.getTitles()) {
					md.update(t.getTitleStr().getBytes("utf-8"));
				}
				
				for (Isbn i: encapsulator.getIsbns()) {
					md.update(i.getIsbn().byteValue());
				}
				
				for (Issn i: encapsulator.getIssns()) {
					md.update(i.getIssn().getBytes());
				}
				
				for (Cnb c: encapsulator.getCnbs()) {
					md.update(c.getCnb().getBytes());
				}
				
				if (encapsulator.getPublicationYear() != null) {
					md.update(encapsulator.getPublicationYear().byteValue());
				}
				
				for (HarvestedRecordFormat hrfe: encapsulator.getFormats()) {
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
				
				for (Oclc o: encapsulator.getOclcs()) {
					md.update(o.getOclcStr().getBytes());
				}
				
				for (String l: encapsulator.getLanguages()) {
					md.update(l.getBytes());
				}
				
				if (encapsulator.getClusterId() != null) {
					md.update(encapsulator.getClusterId().getBytes());
				}
				
				if (encapsulator.getRaw001Id() != null) {
					md.update(encapsulator.getRaw001Id().getBytes());
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
		 * @param hr
		 * @return
		 */
		protected String computeHashValue(final HarvestedRecord hr) {
			DedupKeysencapsulator encapsulator = new DedupKeysencapsulator();
			
			encapsulator.setTitles(hr.getTitles());
			encapsulator.setIsbns(hr.getIsbns());
			encapsulator.setIssns(hr.getIssns());
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
			
			return computeHashValue(encapsulator);
		}
		
	protected class DedupKeysencapsulator {
		List<Title> titles = new ArrayList<>();
		List<Isbn> isbns = new ArrayList<>();
		List<Issn> issns = new ArrayList<>();
		List<Cnb> cnbs = new ArrayList<>();
		List<Oclc> oclcs = new ArrayList<>();
		List<HarvestedRecordFormat> formats = new ArrayList<>();
		Set<String> languages = new HashSet<>();
		
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
	}

}
