package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Ismn.TABLE_NAME)
public class Ismn extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "ismn";
	
	@Column(name="ismn")
	private Long ismn;
	
	@Column(name="order_in_record")
	private Long orderInRecord;
	
	@Column(name="note")
	private String note = "";

	public Long getIsmn() {
		return ismn;
	}

	public void setIsmn(Long ismn) {
		this.ismn = ismn;
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
		result = prime * result + ((ismn == null) ? 0 : ismn.hashCode());
		result = prime * result
				+ ((orderInRecord == null) ? 0 : orderInRecord.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Ismn other = (Ismn) obj;
		if (ismn == null) {
			if (other.ismn != null)
				return false;
		} else if (!ismn.equals(other.ismn))
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
		return "ismn [ismn=" + ismn + ", orderInRecord=" + orderInRecord
				+ ", note=" + note + "]";
	}
}
