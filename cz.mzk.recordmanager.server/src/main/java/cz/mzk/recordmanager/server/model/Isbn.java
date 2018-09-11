package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Isbn.TABLE_NAME)
public class Isbn extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "isbn";

	@Column(name = "harvested_record_id", updatable = false, insertable = false)
	private Long harvestedRecordId;

	@Column(name="isbn")
	private Long isbn;
	
	@Column(name="order_in_record")
	private Long orderInRecord;
	
	@Column(name="note")
	private String note = "";

	public static Isbn create(final long isbn, final long orderInRecord, final String note) {
		Isbn newIsbn = new Isbn();
		newIsbn.setIsbn(isbn);
		newIsbn.setOrderInRecord(orderInRecord);
		newIsbn.setNote(note);
		return newIsbn;
	}

	public Long getIsbn() {
		return isbn;
	}

	public void setIsbn(Long isbn) {
		this.isbn = isbn;
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
		result = prime * result + ((isbn == null) ? 0 : isbn.hashCode());
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
		Isbn other = (Isbn) obj;
		if (isbn == null) {
			if (other.isbn != null)
				return false;
		} else if (!isbn.equals(other.isbn))
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
		return "Isbn [isbn=" + isbn + ", orderInRecord=" + orderInRecord
				+ ", note=" + note + ']';
	}
}
