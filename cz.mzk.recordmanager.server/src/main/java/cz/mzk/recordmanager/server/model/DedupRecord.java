package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=DedupRecord.TABLE_NAME)
public class DedupRecord extends AbstractDomainObject {

	public static final String TABLE_NAME = "dedup_record";

	@Column(name="isbn")
	private String isbn;

	@Column(name="title")
	private String title;

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
	
	@Override
	public String toString() {
		return String.format("DedupRecord[id=%s]", getId());
	}
	
}
