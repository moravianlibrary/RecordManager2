package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=DedupKeys.TABLE_NAME)
public class DedupKeys {
	
	public static final String TABLE_NAME = "dedup_keys";
	
	@Id
	@Column(name="id")
	private Long id;

	@Column(name="isbn")
	private String isbn;
	
	@Column(name="title")
	private String title;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
	
}
