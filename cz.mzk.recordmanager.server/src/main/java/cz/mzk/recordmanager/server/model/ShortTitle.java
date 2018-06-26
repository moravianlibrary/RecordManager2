package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=ShortTitle.TABLE_NAME)
public class ShortTitle extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "short_title";
	
	@Column(name="short_title")
	private String shortTitle = "";
	
	@Column(name="order_in_record")
	private Long orderInRecord;

	@Column(name="similarity_enabled")
	private boolean similarityEnabled;

	public static ShortTitle create(final String shortTitle, final long orderInRecord, final boolean similarity) {
		ShortTitle newShortTitle = new ShortTitle();
		newShortTitle.setShortTitleStr(shortTitle);
		newShortTitle.setOrderInRecord(orderInRecord);
		newShortTitle.setSimilarityEnabled(similarity);
		return newShortTitle;
	}

	public String getShortTitleStr() {
		return shortTitle;
	}

	public void setShortTitleStr(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public Long getOrderInRecord() {
		return orderInRecord;
	}

	public void setOrderInRecord(Long orderInRecord) {
		this.orderInRecord = orderInRecord;
	}

	public boolean isSimilarityEnabled() {
		return similarityEnabled;
	}

	public void setSimilarityEnabled(boolean similarityEnabled) {
		this.similarityEnabled = similarityEnabled;
	}

	@Override
	public String toString() {
		return "ShortTitle [shortTitle=" + shortTitle + ", orderInRecord="
				+ orderInRecord + ", similarityEnabled=" + similarityEnabled
				+ ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((orderInRecord == null) ? 0 : orderInRecord.hashCode());
		result = prime * result + ((shortTitle == null) ? 0 : shortTitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ShortTitle other = (ShortTitle) obj;
		if (orderInRecord == null) {
			if (other.orderInRecord != null)
				return false;
		} else if (!orderInRecord.equals(other.orderInRecord))
			return false;
		if (shortTitle == null) {
			if (other.shortTitle != null)
				return false;
		} else if (!shortTitle.equals(other.shortTitle))
			return false;
		return true;
	}
}
