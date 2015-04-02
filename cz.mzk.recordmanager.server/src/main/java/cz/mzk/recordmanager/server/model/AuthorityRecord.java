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
@Table(name=AuthorityRecord.TABLE_NAME)
public class AuthorityRecord extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "authority_record";
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="oai_harvest_conf_id", nullable=false)
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
	
	@Column(name="authority_type")
	private String authorityType;
	
	@Column(name="oai_record_id")
	private String oaiRecordId;
	
	@Column(name="format")
	private String format;
	
	@Basic(fetch = FetchType.LAZY)
	@Column(name="raw_record") 
	private byte[] rawRecord;

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

	public String getAuthorityType() {
		return authorityType;
	}

	public void setAuthorityType(String authorityType) {
		this.authorityType = authorityType;
	}

	public String getOaiRecordId() {
		return oaiRecordId;
	}

	public void setOaiRecordId(String oaiRecordId) {
		this.oaiRecordId = oaiRecordId;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public byte[] getRawRecord() {
		return rawRecord;
	}

	public void setRawRecord(byte[] rawRecord) {
		this.rawRecord = rawRecord;
	}

}
