package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Title.TABLE_NAME)
public class Title extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "title";
	
	@Column(name="title")
	private String title = "";
	
	@Column(name="order_in_record")
	private Long orderInRecord;

	@Column(name="similarity_enabled")
	private boolean similarityEnabled;

	public static Title create(final String title, final long orderInRecord, final boolean similarity) {
		Title newTitle = new Title();
		newTitle.setTitleStr(title);
		newTitle.setOrderInRecord(orderInRecord);
		newTitle.setSimilarityEnabled(similarity);
		return newTitle;
	}

	public static Title create(final String title, final long orderInRecord) {
		Title newTitle = new Title();
		newTitle.setTitleStr(title);
		newTitle.setOrderInRecord(orderInRecord);
		newTitle.setSimilarityEnabled(MetadataUtils.similarityEnabled(title));
		return newTitle;
	}

	public String getTitleStr() {
		return title;
	}

	public void setTitleStr(String title) {
		this.title = title;
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
		return "Title [title=" + title + ", orderInRecord=" + orderInRecord
				+ ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((orderInRecord == null) ? 0 : orderInRecord.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Title other = (Title) obj;
		if (orderInRecord == null) {
			if (other.orderInRecord != null)
				return false;
		} else if (!orderInRecord.equals(other.orderInRecord))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
