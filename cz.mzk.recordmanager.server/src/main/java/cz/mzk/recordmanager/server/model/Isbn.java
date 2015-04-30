package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Isbn.TABLE_NAME)
public class Isbn extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "isbn";
	
	@Column(name="isbn")
	private Long isbn;
	
	@Column(name="order_in_record")
	private Long orderInRecord;
	
	@Column(name="note")
	private String note = "";

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
	public String toString() {
		return "Isbn [isbn=" + isbn + ", orderInRecord=" + orderInRecord
				+ ", note=" + note + "]";
	}
}
