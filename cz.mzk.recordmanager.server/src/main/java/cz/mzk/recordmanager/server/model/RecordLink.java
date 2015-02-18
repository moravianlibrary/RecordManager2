package cz.mzk.recordmanager.server.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.common.base.Preconditions;

@Entity
@Table(name = RecordLink.TABLE_NAME)
public class RecordLink {

	public static final String TABLE_NAME = "record_link";

	@Embeddable
	public static class RecordLinkId implements Serializable {

		private static final long serialVersionUID = 1L;

		@ManyToOne(optional = false)
		@JoinColumn(name = "harvested_record_id", nullable = false)
		private HarvestedRecord harvestedRecord;

		@ManyToOne(optional = false)
		@JoinColumn(name = "dedup_record_id", nullable = false)
		private DedupRecord dedupRecord;
		
		public RecordLinkId() {
		}

		public RecordLinkId(HarvestedRecord harvestedRecord,
				DedupRecord dedupRecord) {
			super();
			Preconditions.checkNotNull(harvestedRecord, "harvestedRecord");
			Preconditions.checkNotNull(dedupRecord, "dedupRecord");
			this.harvestedRecord = harvestedRecord;
			this.dedupRecord = dedupRecord;
		}

		public HarvestedRecord getHarvestedRecord() {
			return harvestedRecord;
		}

		public DedupRecord getDedupRecord() {
			return dedupRecord;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(harvestedRecord, dedupRecord);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			RecordLinkId other = (RecordLinkId) obj;
			return Objects.equals(this.getHarvestedRecord(), other.getHarvestedRecord()) &&
					Objects.equals(this.getDedupRecord(), other.getDedupRecord());
		}

	}
	
	@Embedded
	@Id
	private RecordLinkId id;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date created = new Date();
	
	public RecordLink() {
	}
	
	public RecordLink(HarvestedRecord harvestedRecord, DedupRecord dedupRecord) {
		this.id = new RecordLinkId(harvestedRecord, dedupRecord);
	}

	public HarvestedRecord getHarvestedRecord() {
		return id.getHarvestedRecord();
	}

	public DedupRecord getDedupRecord() {
		return id.getDedupRecord();
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		RecordLink other = (RecordLink) obj;
		return Objects.equals(this.id, other.id);
	}

}
