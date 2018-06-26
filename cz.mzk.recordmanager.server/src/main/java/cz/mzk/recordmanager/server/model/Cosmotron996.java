package cz.mzk.recordmanager.server.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name=Cosmotron996.TABLE_NAME)
public class Cosmotron996 extends AbstractDomainObject{
	
	public static final String TABLE_NAME = "cosmotron_996";
	
	@Column(name="import_conf_id")
	private Long harvestedFrom;
	
	@Column(name="record_id")
	private String recordId;

	@Column(name="parent_record_id")
	private String parentRecordId;

	@Column(name="harvested")
	@Temporal(TemporalType.TIMESTAMP)
	private Date harvested = new Date();
	
	@Column(name="updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated = harvested;
	
	@Column(name="deleted")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deleted;
	
	@Basic(fetch = FetchType.LAZY)
	@Column(name="raw_record") 
	private byte[] rawRecord;

	public Cosmotron996() {

	}

	public Cosmotron996(String recordId, Long configId) {
		this.recordId = recordId;
		this.harvestedFrom = configId;
	}

	public Long getHarvestedFrom() {
		return harvestedFrom;
	}

	public void setHarvestedFrom(Long harvestedFromId) {
		this.harvestedFrom = harvestedFromId;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
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

	public byte[] getRawRecord() {
		return rawRecord;
	}

	public void setRawRecord(byte[] rawRecord) {
		this.rawRecord = rawRecord;
	}

	public String getParentRecordId() {
		return parentRecordId;
	}

	public void setParentRecordId(String parentRecordId) {
		this.parentRecordId = parentRecordId;
	}

	@Override
	public String toString() {
		return "Cosmotron996 [harvestedFrom=" + harvestedFrom + ", recordId=" + recordId
				+ ", parentRecordId=" + parentRecordId + ", harvested="
				+ harvested + ", updated=" + updated + ", deleted=" + deleted
				+ ']';
	}


}
