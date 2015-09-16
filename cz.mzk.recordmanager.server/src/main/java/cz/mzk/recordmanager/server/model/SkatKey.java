package cz.mzk.recordmanager.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=SkatKey.TABLE_NAME)
public class SkatKey {

	public static final String TABLE_NAME = "skat_keys";
	
	@Embeddable
	public static class SkatKeyCompositeId implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@Column(name="skat_record_id")
		private Long skatHarvestedRecordId;
		
		@Column(name="sigla")
		private String sigla;
		
		@Column(name="local_record_id")
		private String recordId;

		public SkatKeyCompositeId(Long skatHarvestedRecordId, String sigla,
				String recordId) {
			super();
			this.skatHarvestedRecordId = skatHarvestedRecordId;
			this.sigla = sigla;
			this.recordId = recordId;
		}
		
		public SkatKeyCompositeId() {
			super();
		}

		public Long getSkatHarvestedRecordId() {
			return skatHarvestedRecordId;
		}

		public String getSigla() {
			return sigla;
		}

		public String getRecordId() {
			return recordId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((recordId == null) ? 0 : recordId.hashCode());
			result = prime * result + ((sigla == null) ? 0 : sigla.hashCode());
			result = prime
					* result
					+ ((skatHarvestedRecordId == null) ? 0
							: skatHarvestedRecordId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SkatKeyCompositeId other = (SkatKeyCompositeId) obj;
			if (recordId == null) {
				if (other.recordId != null)
					return false;
			} else if (!recordId.equals(other.recordId))
				return false;
			if (sigla == null) {
				if (other.sigla != null)
					return false;
			} else if (!sigla.equals(other.sigla))
				return false;
			if (skatHarvestedRecordId == null) {
				if (other.skatHarvestedRecordId != null)
					return false;
			} else if (!skatHarvestedRecordId
					.equals(other.skatHarvestedRecordId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SkatKeyCompositeId [skatHarvestedRecordId="
					+ skatHarvestedRecordId + ", sigla=" + sigla
					+ ", recordId=" + recordId + "]";
		}
		
	}
	
	@EmbeddedId
	private SkatKeyCompositeId skatKeyId;
	
	@Column(name = "manually_merged")
	private boolean manuallyMerged = false;
	
	public SkatKey() {}

	public SkatKey(SkatKeyCompositeId skatKeyId) {
		super();
		this.skatKeyId = skatKeyId;
	}

	public SkatKeyCompositeId getSkatKeyId() {
		return skatKeyId;
	}

	public void setSkatKeyId(SkatKeyCompositeId skatKeyId) {
		this.skatKeyId = skatKeyId;
	}

	public boolean isManuallyMerged() {
		return manuallyMerged;
	}

	public void setManuallyMerged(boolean manuallyMerged) {
		this.manuallyMerged = manuallyMerged;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((skatKeyId == null) ? 0 : skatKeyId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SkatKey other = (SkatKey) obj;
		if (skatKeyId == null) {
			if (other.skatKeyId != null)
				return false;
		} else if (!skatKeyId.equals(other.skatKeyId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SkatKey [skatKeyId=" + skatKeyId + "]";
	}
	
}
