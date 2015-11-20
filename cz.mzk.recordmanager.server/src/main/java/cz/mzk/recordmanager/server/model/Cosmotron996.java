package cz.mzk.recordmanager.server.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name=Cosmotron996.TABLE_NAME)
public class Cosmotron996 extends AbstractDomainObject{
	
	public static final String TABLE_NAME = "cosmotron_996";

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="harvested_record_id", nullable=false, updatable=false, insertable=false)
	private HarvestedRecord harvestedRecord;
	
	@Column(name="import_conf_id")
	private Long harvestedFrom;
	
	@Column(name="record_id")
	private String recordId;
	
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

	public HarvestedRecord getHarvestedRecord() {
		return harvestedRecord;
	}

	public void setHarvestedRecord(HarvestedRecord harvestedRecord) {
		this.harvestedRecord = harvestedRecord;
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

	@Override
	public String toString() {
		return "Cosmotron996 [harvestedRecordId=" + harvestedRecord
				+ ", harvestedFromId=" + harvestedFrom + ", recordId="
				+ recordId + ", harvested=" + harvested + ", updated="
				+ updated + ", deleted=" + deleted + "]";
	}


}
