package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Ean.TABLE_NAME)
public class Ean extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "ean";

	@Column(name = "harvested_record_id", updatable = false, insertable = false)
	private Long harvestedRecordId;

	@Column(name="ean")
	private Long ean;
	
	@Column(name="order_in_record")
	private Long orderInRecord;
	
	@Column(name="note")
	private String note = "";

	public static Ean create(final Long ean, final long orderInRecord, final String note) {
		Ean newEan = new Ean();
		newEan.setEan(ean);
		newEan.setOrderInRecord(orderInRecord);
		newEan.setNote(MetadataUtils.shorten(note, 300));
		return newEan;
	}

	public Long getEan() {
		return ean;
	}

	public void setEan(Long ean) {
		this.ean = ean;
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
		result = prime * result + ((ean == null) ? 0 : ean.hashCode());
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
		Ean other = (Ean) obj;
		if (ean == null) {
			if (other.ean != null)
				return false;
		} else if (!ean.equals(other.ean))
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
		return "Ean [ean=" + ean + ", orderInRecord=" + orderInRecord
				+ ", note=" + note + ']';
	}
}
