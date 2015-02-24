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

	public static final String TABLE_NAME = "dedup_record";

	@Column(name="isbn")
	private String isbn;

	@Column(name="title")
	private String title;
	
	@Column(name="publication_year")
	private Long publicationYear;
	
	@Column(name="physical_format")
	private String physicalFormat;
	
	@Column(name="updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated = new Date();

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(Long publicationYear) {
		this.publicationYear = publicationYear;
	}

	public String getPhysicalFormat() {
		return physicalFormat;
	}

	public void setPhysicalFormat(String physicalFormat) {
		this.physicalFormat = physicalFormat;
	}

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
