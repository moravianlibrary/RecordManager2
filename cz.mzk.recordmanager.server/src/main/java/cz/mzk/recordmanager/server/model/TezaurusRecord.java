package cz.mzk.recordmanager.server.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = TezaurusRecord.TABLE_NAME)
public class TezaurusRecord extends AbstractDomainObject {

	public static final String TABLE_NAME = "tezaurus_record";

	@Column(name = "record_id")
	private String recordId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "import_conf_id", referencedColumnName = "id", nullable = false)
	private ImportConfiguration harvestedFrom;

	@Embeddable
	public static class TezaurusKey {

		@Column(name = "source_field")
		private String sourceField;

		@Column(name = "name")
		private String name;

		public TezaurusKey() {
		}

		public TezaurusKey(String source, String name) {
			this.sourceField = source;
			this.name = name;
		}

		public String getSourceField() {
			return sourceField;
		}

		public void setSourceField(String sourceField) {
			this.sourceField = sourceField;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "TezaurusKey [sourceField=" + sourceField + ", name=" + name
					+ ']';
		}
	}

	@Embedded
	private TezaurusKey tezaurusKey;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "raw_record")
	private byte[] rawRecord;

	public TezaurusRecord() {
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public ImportConfiguration getHarvestedFrom() {
		return harvestedFrom;
	}

	public void setHarvestedFrom(ImportConfiguration harvestedFrom) {
		this.harvestedFrom = harvestedFrom;
	}

	public byte[] getRawRecord() {
		return rawRecord;
	}

	public void setRawRecord(byte[] rawRecord) {
		this.rawRecord = rawRecord;
	}

	public TezaurusKey getTezaurusKey() {
		return tezaurusKey;
	}

	public void setTezaurusKey(TezaurusKey tezaurusKey) {
		this.tezaurusKey = tezaurusKey;
	}

	@Override
	public String toString() {
		return "TezaurusRecord [recordId=" + recordId + ", harvestedFrom="
				+ harvestedFrom + ", tezaurusKey=" + tezaurusKey + ']';
	}

}
