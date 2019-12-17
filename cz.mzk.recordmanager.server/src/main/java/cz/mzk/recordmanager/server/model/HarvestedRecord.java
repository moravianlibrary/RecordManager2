package cz.mzk.recordmanager.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.google.common.base.Preconditions;


@Entity
@Table(name=HarvestedRecord.TABLE_NAME)
public class HarvestedRecord extends AbstractDomainObject {

	public static final String TABLE_NAME = "harvested_record";
	
	@Embeddable
	public static class HarvestedRecordUniqueId implements Serializable {

		private static final long serialVersionUID = 1L;
		
		@Column(name="import_conf_id")
		private Long harvestedFromId;

		@Column(name="record_id")
		private String recordId;

		// for hibernate
		protected HarvestedRecordUniqueId() {
		}
		
		public HarvestedRecordUniqueId(ImportConfiguration harvestedFrom,
				String recordId) {
			super();
			Preconditions.checkNotNull(harvestedFrom, "harvestedFrom");
			Preconditions.checkNotNull(recordId, "recordId");
			this.harvestedFromId = harvestedFrom.getId();
			this.recordId = recordId;
		}
		
		public HarvestedRecordUniqueId(Long harvestedFromId,
				String recordId) {
			super();
			Preconditions.checkNotNull(harvestedFromId, "harvestedFromId");
			Preconditions.checkNotNull(recordId, "recordId");
			this.harvestedFromId = harvestedFromId;
			this.recordId = recordId;
		}

		public Long getHarvestedFromId() {
			return harvestedFromId;
		}

		public String getRecordId() {
			return recordId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(harvestedFromId, recordId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}	
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			HarvestedRecordUniqueId other = (HarvestedRecordUniqueId) obj;
			return Objects.equals(this.getHarvestedFromId(), other.getHarvestedFromId())
					&& Objects.equals(this.getRecordId(), other.getRecordId());
		}

		@Override
		public String toString() {
			return String.format("%s[harvestedFromId=%s, recordId=%s]",
					getClass().getSimpleName(), harvestedFromId, recordId);
		}

	}
	
	@Embedded
	private HarvestedRecordUniqueId uniqueId;
	
	@Column(name="raw_001_id")
	private String raw001Id;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="import_conf_id", nullable=false, updatable=false, insertable=false)
	private ImportConfiguration harvestedFrom;

	@Column(name="harvested")
	@Temporal(TemporalType.TIMESTAMP)
	private Date harvested = new Date();
	
	@Column(name="updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated = harvested;
	@Column(name="last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = harvested;
	@Column(name="deleted")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deleted;
	
	@Column(name="oai_timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date oaiTimestamp;
	
	@Column(name="format")
	private String format;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<Isbn> isbns = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<Ean> eans = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<PublisherNumber> publisherNumbers = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<Issn> issns = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<Cnb> cnb = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<Title> titles = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<ShortTitle> shortTitles = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<AnpTitle> anpTitles = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<Oclc> oclcs = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<Ismn> ismns = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<BiblioLinkerSimilar> biblioLinkerSimilarUrls = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id", nullable=false)
	private List<BLTitle> blTitles = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "harvested_record_id", referencedColumnName = "id", nullable = false)
	private List<BlCommonTitle> blCommonTitle = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "harvested_record_id", referencedColumnName = "id", nullable = false)
	private List<BLEntity> blEntity = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "harvested_record_id", referencedColumnName = "id", nullable = false)
	private List<BLTopicKey> blTopicKey = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id")
	private List<FulltextKramerius> fulltextKramerius = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="harvested_record_id", referencedColumnName="id")
	private List<Inspiration> inspiration = new ArrayList<>();

	@OneToMany(mappedBy="id.harvestedRecordId", cascade = CascadeType.ALL, orphanRemoval=true)
	@MapKey(name="id.langStr")
	private Map<String, Language> languages = new HashMap<>();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "harvested_record_id", referencedColumnName = "id", nullable = false)
	private List<BLLanguage> blLanguages = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "harvested_record_id", referencedColumnName = "id")
	private List<Authority> authorities = new ArrayList<>();

	// TODO consider moving dedup keys to separate table
	@Column(name="author_auth_key")
	private String authorAuthKey;
	
	@Column(name="author_string")
	private String authorString;
	
	@Column(name="issn_series")
	private String issnSeries;
	
	@Column(name="issn_series_order")
	private String issnSeriesOrder;
	
	@Column(name="uuid")
	private String uuid;
	
	@Column(name="scale")
	private Long scale;
	
	@Column(name="publication_year")
	private Long publicationYear;
	@Column(name = "publisher")
	private String publisher;

	@Column(name = "edition")
	private String edition;

	@Column(name = "disadvantaged")
	private boolean disadvantaged = true;

	@OneToMany
	@JoinTable(
	   name = "harvested_record_format_link", 
	   joinColumns = @JoinColumn(name = "harvested_record_id "), 
	   inverseJoinColumns = @JoinColumn(name = "harvested_record_format_id "))
	private List<HarvestedRecordFormat> physicalFormats = new ArrayList<>();
	
	@Basic(fetch = FetchType.LAZY)
	@Column(name="raw_record") 
	private byte[] rawRecord;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="dedup_record_id", nullable=true)
	private DedupRecord dedupRecord;
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="biblio_linker_id", nullable=true)
	private BiblioLinker biblioLinker;

	@Column(name="biblio_linker_similar")
	private boolean biblioLinkerSimilar = false;
	@Column(name="weight")
	private Long weight;
	
	@Column(name="cluster_id")
	private String clusterId;
	
	@Column(name="pages")
	private Long pages;
	
	@Column(name="dedup_keys_hash")
	private String dedupKeysHash = "";
	
	@Column(name="next_dedup_flag")
	private boolean nextDedupFlag = true;

	@Column(name="source_info_t")
	private String sourceInfoT;

	@Column(name="source_info_x")
	private String sourceInfoX;

	@Column(name="source_info_g")
	private String sourceInfoG;

	@Column(name="upv_application_id")
	private String upvApplicationId;

	@Column(name = "sigla")
	private String sigla;
	@Column(name="next_biblio_linker_flag")
	private boolean nextBiblioLinkerFlag = true;

	@Column(name = "next_biblio_linker_similar_flag")
	private boolean nextBiblioLinkerSimilarFlag = true;

	@Column(name = "biblio_linker_keys_hash")
	private String biblioLinkerKeysHash = "";

	@Column(name = "bl_disadvantaged")
	private boolean blDisadvantaged = true;

	@Column(name = "bl_author")
	private String blAuthor;

	@Column(name = "bl_publisher")
	private String blPublisher;

	@Column(name = "bl_series")
	private String blSeries;

	/**
	 * indicator variable used for filtering reasons
	 */
	@Transient
	private boolean shouldBeProcessed = true;
	
	/**
	 * Temporal indicator variable used for deduplication 
	 */
	@Transient
	private Date temporalOldOaiTimestamp;
	
	/**
	 * Temporal indicator variable used for deduplication
	 */
	@Transient
	private String temporalDedupHash;
	/**
	 * Temporal indicator variable used for biblio linker
	 */
	@Transient
	private String temporalBiblioLinkerHash;
	
	public HarvestedRecord() {
	}
	
	public HarvestedRecord(HarvestedRecordUniqueId id) {
		super();
		this.uniqueId = id;
	}

	public HarvestedRecordUniqueId getUniqueId() {
		return uniqueId;
	}

	public void setId(HarvestedRecordUniqueId id) {
		this.uniqueId = id;
	}

	public ImportConfiguration getHarvestedFrom() {
		return harvestedFrom;
	}

	public void setHarvestedFrom(ImportConfiguration harvestedFrom) {
		this.harvestedFrom = harvestedFrom;
	}

	public Date getHarvested() {
		return harvested;
	}

	public void setHarvested(Date harvested) {
		this.harvested = harvested;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Date getDeleted() {
		return deleted;
	}

	public void setDeleted(Date deleted) {
		this.deleted = deleted;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public List<Isbn> getIsbns() {
		return isbns;
	}

	public void setIsbns(List<Isbn> isbns) {
		this.isbns = isbns;
	}

	public List<Ismn> getIsmns() {
		return ismns;
	}

	public void setIsmns(List<Ismn> ismns) {
		this.ismns = ismns;
	}

	public List<Title> getTitles() {
		return titles;
	}

	public void setTitles(List<Title> titles) {
		this.titles = titles;
	}
	
	public Long getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(Long publicationYear) {
		this.publicationYear = publicationYear;
	}

	public byte[] getRawRecord() {
		return rawRecord;
	}

	public void setRawRecord(byte[] rawRecord) {
		this.rawRecord = rawRecord;
	}

	public DedupRecord getDedupRecord() {
		return dedupRecord;
	}

	public void setDedupRecord(DedupRecord dedupRecord) {
		this.dedupRecord = dedupRecord;
	}

	public List<HarvestedRecordFormat> getPhysicalFormats() {
		return physicalFormats;
	}

	public void setPhysicalFormats(List<HarvestedRecordFormat> physicalFormats) {
		this.physicalFormats = physicalFormats;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public String getAuthorAuthKey() {
		return authorAuthKey;
	}

	public void setAuthorAuthKey(String authorAuthKey) {
		this.authorAuthKey = authorAuthKey;
	}

	public String getAuthorString() {
		return authorString;
	}

	public void setAuthorString(String authorString) {
		this.authorString = authorString;
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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getScale() {
		return scale;
	}

	public void setScale(Long scale) {
		this.scale = scale;
	}

	public List<Issn> getIssns() {
		return issns;
	}

	public void setIssns(List<Issn> issns) {
		this.issns = issns;
	}

	public List<Cnb> getCnb() {
		return cnb;
	}

	public void setCnb(List<Cnb> cnb) {
		this.cnb = cnb;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		if(clusterId != null && clusterId.length() <= 20) this.clusterId = clusterId;
		else this.clusterId = null;
	}

	public Long getPages() {
		return pages;
	}

	public void setPages(Long pages) {
		this.pages = pages;
	}

	public List<Oclc> getOclcs() {
		return oclcs;
	}

	public void setOclcs(List<Oclc> oclcs) {
		this.oclcs = oclcs;
	}

	public Set<String> getLanguages() {
		return languages.keySet();
	}
	
	public void setLanguages(List<String> newLangs) {
		for (String newLang : newLangs) {
			if (!languages.containsKey(newLang)) {
				languages.put(newLang, new Language(this, newLang));
			}
		}
		Iterator<String> iter = languages.keySet().iterator();
		while (iter.hasNext()) {
			String lang = iter.next();
			if (!newLangs.contains(lang)) {
				iter.remove();
			}
		}
	}

	public boolean getShouldBeProcessed() {
		return shouldBeProcessed;
	}

	public void setShouldBeProcessed(boolean shouldBeProcessed) {
		this.shouldBeProcessed = shouldBeProcessed;
	}

	public List<FulltextKramerius> getFulltextKramerius() {
		return fulltextKramerius;
	}

	public void setFulltextKramerius(List<FulltextKramerius> fulltextKramerius) {
		this.fulltextKramerius = fulltextKramerius;
	}
	

	public String getRaw001Id() {
		return raw001Id;
	}

	public void setRaw001Id(String raw001Id) {
		this.raw001Id = raw001Id;
	}

	@Override
	public String toString() {
		return String.format("HarvestedRecord[id=%s, uniqueId=%s]", getId(), getUniqueId());
	}

	public String getDedupKeysHash() {
		return dedupKeysHash;
	}

	public void setDedupKeysHash(String dedupKeysHash) {
		this.dedupKeysHash = dedupKeysHash;
	}

	public boolean isNextDedupFlag() {
		return nextDedupFlag;
	}

	public void setNextDedupFlag(boolean nextDedupFlag) {
		this.nextDedupFlag = nextDedupFlag;
	}

	public Date getOaiTimestamp() {
		return oaiTimestamp;
	}

	public void setOaiTimestamp(Date oaiTimestamp) {
		this.oaiTimestamp = oaiTimestamp;
	}

	public Date getTemporalOldOaiTimestamp() {
		return temporalOldOaiTimestamp;
	}

	public void setTemporalOldOaiTimestamp(Date oldOaiTimestamp) {
		this.temporalOldOaiTimestamp = oldOaiTimestamp;
	}
	
	public String getTemporalDedupHash() {
		return temporalDedupHash;
	}

	public void setTemporalDedupHash(String temporalDedupHash) {
		this.temporalDedupHash = temporalDedupHash;
	}

	public String getTemporalBiblioLinkerHash() {
		return temporalBiblioLinkerHash;
	}

	public void setTemporalBiblioLinkerHash(String temporalBiblioLinkerHash) {
		this.temporalBiblioLinkerHash = temporalBiblioLinkerHash;
	}

	public List<Inspiration> getInspiration() {
		return inspiration;
	}

	public void setInspiration(List<Inspiration> inspiration) {
		this.inspiration = inspiration;
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

	public String getUpvApplicationId() {
		return upvApplicationId;
	}

	public void setUpvApplicationId(String upvApplicationId) {
		this.upvApplicationId = upvApplicationId;
	}

	public String getSourceInfoX() {
		return sourceInfoX;
	}

	public void setSourceInfoX(String sourceInfoX) {
		this.sourceInfoX = sourceInfoX;
	}

	public String getSourceInfoG() {
		return sourceInfoG;
	}

	public void setSourceInfoG(String sourceInfoG) {
		this.sourceInfoG = sourceInfoG;
	}

	public String getSourceInfoT() {

		return sourceInfoT;
	}

	public void setSourceInfoT(String sourceInfoT) {
		this.sourceInfoT = sourceInfoT;
	}

	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

	public List<Authority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
	}

	public Date getLastHarvest() {
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public boolean isDisadvantaged() {
		return disadvantaged;
	}

	public void setDisadvantaged(boolean disadvantaged) {
		this.disadvantaged = disadvantaged;
	}

	public List<AnpTitle> getAnpTitles() {
		return anpTitles;
	}

	public void setAnpTitles(List<AnpTitle> anpTitles) {
		this.anpTitles = anpTitles;
	}

	public BiblioLinker getBiblioLinker() {
		return biblioLinker;
	}

	public void setBiblioLinker(BiblioLinker biblioLinker) {
		this.biblioLinker = biblioLinker;
	}

	public boolean isBiblioLinkerSimilar() {
		return biblioLinkerSimilar;
	}

	public void setBiblioLinkerSimilar(boolean biblioLinkerSimilar) {
		this.biblioLinkerSimilar = biblioLinkerSimilar;
	}

	public List<BiblioLinkerSimilar> getBiblioLinkerSimilarUrls() {
		return biblioLinkerSimilarUrls;
	}

	public void setBiblioLinkerSimilarUrls(List<BiblioLinkerSimilar> biblioLinkerSimilarUrls) {
		this.biblioLinkerSimilarUrls = biblioLinkerSimilarUrls;
	}

	public boolean isNextBiblioLinkerFlag() {
		return nextBiblioLinkerFlag;
	}

	public void setNextBiblioLinkerFlag(boolean nextBiblioLinkerFlag) {
		this.nextBiblioLinkerFlag = nextBiblioLinkerFlag;
	}

	public List<BLTitle> getBlTitles() {
		return blTitles;
	}

	public void setBlTitles(List<BLTitle> blTitles) {
		this.blTitles = blTitles;
	}

	public String getBlAuthor() {
		return blAuthor;
	}

	public void setBlAuthor(String blAuthor) {
		this.blAuthor = blAuthor;
	}

	public String getBlPublisher() {
		return blPublisher;
	}

	public void setBlPublisher(String blPublisher) {
		this.blPublisher = blPublisher;
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

	public List<BLEntity> getBlEntity() {
		return blEntity;
	}

	public void setBlEntity(List<BLEntity> blEntity) {
		this.blEntity = blEntity;
	}

	public List<BLTopicKey> getBlTopicKey() {
		return blTopicKey;
	}

	public void setBlTopicKey(List<BLTopicKey> blTopicKey) {
		this.blTopicKey = blTopicKey;
	}

	public List<BLLanguage> getBlLanguages() {
		return blLanguages;
	}

	public void setBlLanguages(List<BLLanguage> blLanguages) {
		this.blLanguages = blLanguages;
	}

	public boolean isNextBiblioLinkerSimilarFlag() {
		return nextBiblioLinkerSimilarFlag;
	}

	public void setNextBiblioLinkerSimilarFlag(boolean nextBiblioLinkerSimilarFlag) {
		this.nextBiblioLinkerSimilarFlag = nextBiblioLinkerSimilarFlag;
	}

	public String getBiblioLinkerKeysHash() {
		return biblioLinkerKeysHash;
	}

	public void setBiblioLinkerKeysHash(String biblioLinkerKeysHash) {
		this.biblioLinkerKeysHash = biblioLinkerKeysHash;
	}

	public boolean isBlDisadvantaged() {
		return blDisadvantaged;
	}

	public void setBlDisadvantaged(boolean blDisadvantaged) {
		this.blDisadvantaged = blDisadvantaged;
	}
}
