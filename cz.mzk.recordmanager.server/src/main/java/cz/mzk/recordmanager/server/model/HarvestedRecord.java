package cz.mzk.recordmanager.server.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name=HarvestedRecord.TABLE_NAME)
public class HarvestedRecord extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "harvested_record";
	
	@ManyToOne(optional=false)
	@JoinColumn(name="oai_harvest_conf_id", nullable=false)
	private OAIHarvestConfiguration harvestedFrom;
	
	@Column(name="oai_record_id")
	private String oaiRecordId;
	
	@Column(name="deleted")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deleted;
	
	@Basic(fetch = FetchType.LAZY)
	@Column(name="raw_record")
	@Lob
	private byte[] rawRecord;

	public OAIHarvestConfiguration getHarvestedFrom() {
		return harvestedFrom;
	}

	public void setHarvestedFrom(OAIHarvestConfiguration harvestedFrom) {
		this.harvestedFrom = harvestedFrom;
	}

	public String getOaiRecordId() {
		return oaiRecordId;
	}

	public void setOaiRecordId(String oaiRecordId) {
		this.oaiRecordId = oaiRecordId;
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

}
