package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Issn.TABLE_NAME)
public class Issn extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "issn";
	
	@Column(name="issn")
	private String issn;
	
	@Column(name="order_in_record")
	private Long orderInRecord;
	
	@Column(name="note")
	private String note = "";

	public String getIssn() {
		return issn;
	}

	public void setIssn(String issn) {
		this.issn = issn;
	}

	public Long getOrderInRecord() {
		return orderInRecord;
	}

	public void setOrderInRecord(Long orderInRecord) {
		this.orderInRecord = orderInRecord;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((issn == null) ? 0 : issn.hashCode());
		result = prime * result
				+ ((orderInRecord == null) ? 0 : orderInRecord.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Issn other = (Issn) obj;
		if (issn == null) {
			if (other.issn != null)
				return false;
		} else if (!issn.equals(other.issn))
			return false;
		if (orderInRecord == null) {
			if (other.orderInRecord != null)
				return false;
		} else if (!orderInRecord.equals(other.orderInRecord))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Issn [issn=" + issn + ", orderInRecord=" + orderInRecord
				+ ", note=" + note + "]";
	}	
}
