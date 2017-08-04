package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = PublisherNumber.TABLE_NAME)
public class PublisherNumber extends AbstractDomainObject {

	public static final String TABLE_NAME = "publisher_number";

	protected PublisherNumber() {
	}

	public PublisherNumber(String publisherNumber, Long orderInRecord) {
		super();
		this.publisherNumber = publisherNumber;
		this.orderInRecord = orderInRecord;
	}

	@Column(name = "publisher_number")
	private String publisherNumber;

	@Column(name = "order_in_record")
	private Long orderInRecord;

	public String getPublisherNumber() {
		return publisherNumber;
	}

	public void setPublisherNumber(String publisher_number) {
		this.publisherNumber = publisher_number;
	}

	public Long getOrderInRecord() {
		return orderInRecord;
	}

	public void setOrderInRecord(Long orderInRecord) {
		this.orderInRecord = orderInRecord;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((publisherNumber == null) ? 0 : publisherNumber.hashCode());
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
		PublisherNumber other = (PublisherNumber) obj;
		if (publisherNumber == null) {
			if (other.publisherNumber != null)
				return false;
		} else if (!publisherNumber.equals(other.publisherNumber))
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
		return "PublisherNumber [publisherNumber=" + publisherNumber
				+ ", orderInRecord=" + orderInRecord + "]";
	}

}
