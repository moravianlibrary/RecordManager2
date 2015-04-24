package cz.mzk.recordmanager.server.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.google.common.base.Preconditions;


@Entity
@Table(name=HarvestedRecord.TABLE_NAME)
public class HarvestedRecord {
	
	public static final String TABLE_NAME = "harvested_record";
	
	@Embeddable
	public static class HarvestedRecordId implements Serializable {

		private static final long serialVersionUID = 1L;
		
		@Column(name="oai_harvest_conf_id")
		private Long harvestedFromId;

		@Column(name="record_id")
		private String recordId;

		// for hibernate
		private HarvestedRecordId() {
		}
		
		public HarvestedRecordId(OAIHarvestConfiguration harvestedFrom,
				String recordId) {
			super();
			Preconditions.checkNotNull(harvestedFrom, "harvestedFrom");
			Preconditions.checkNotNull(recordId, "recordId");
			this.harvestedFromId = harvestedFrom.getId();
			this.recordId = recordId;
		}
		
		public HarvestedRecordId(Long harvestedFromId,
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
			HarvestedRecordId other = (HarvestedRecordId) obj;
			return Objects.equals(this.getHarvestedFromId(), other.getHarvestedFromId())
					&& Objects.equals(this.getRecordId(), other.getRecordId());
		}

		@Override
		public String toString() {
			return String.format("%s[harvestedFromId=%s, recordId=%s]",
					getClass().getSimpleName(), harvestedFromId, recordId);
		}

	}
	
	@Id
	@Embedded
	private HarvestedRecordId id;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="oai_harvest_conf_id", nullable=false, updatable=false, insertable=false)
	private OAIHarvestConfiguration harvestedFrom;

	@Column(name="harvested")
	@Temporal(TemporalType.TIMESTAMP)
	private Date harvested = new Date();
	
	@Column(name="updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;
	
	@Column(name="deleted")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deleted;
	
	@Column(name="format")
	private String format;
	
	@Column(name="isbn")
	private String isbn;
	
	@Column(name="title")
	private String title;
	
	@Column(name="publication_year")
	private Long publicationYear;
	
	@Column(name="physical_format")
	private String physicalFormat;
	
	@Basic(fetch = FetchType.LAZY)
	@Column(name="raw_record") 
	private byte[] rawRecord;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="dedup_record_id", nullable=true)
	private DedupRecord dedupRecord;
	
	private HarvestedRecord() {
	}
	
	public HarvestedRecord(HarvestedRecordId id) {
		super();
		this.id = id;
	}

	public HarvestedRecordId getId() {
		return id;
	}

	public void setId(HarvestedRecordId id) {
		this.id = id;
	}

	public OAIHarvestConfiguration getHarvestedFrom() {
		return harvestedFrom;
	}

	public void setHarvestedFrom(OAIHarvestConfiguration harvestedFrom) {
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

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(Long publicationYear) {
		this.publicationYear = publicationYear;
	}

	public String getPhysicalFormat() {
		return physicalFormat;
	}

	public void setPhysicalFormat(String physicalFormat) {
		this.physicalFormat = physicalFormat;
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

	@Override
	public String toString() {
		return String.format("HarvestedRecord[id=%s]", getId());
	}

}
