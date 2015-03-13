package cz.mzk.recordmanager.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name=DedupRecord.TABLE_NAME)
public class DedupRecord extends AbstractDomainObject {

//	TODO consider usage of database sequence in id
	public static final String TABLE_NAME = "dedup_record";

	@Column(name="updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated = new Date();

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Override
	public String toString() {
		return String.format("DedupRecord[id=%s]", getId());
	}

}
