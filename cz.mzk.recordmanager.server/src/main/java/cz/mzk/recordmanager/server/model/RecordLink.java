package cz.mzk.recordmanager.server.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

	}
	
	@Embedded
	@Id
	private RecordLinkId id;
	
	public RecordLink(HarvestedRecord harvestedRecord, DedupRecord dedupRecord) {
		this.id = new RecordLinkId(harvestedRecord, dedupRecord);
	}

	public HarvestedRecord getHarvestedRecord() {
		return id.getHarvestedRecord();
	}

	public DedupRecord getDedupRecord() {
		return id.getDedupRecord();
	}

}
