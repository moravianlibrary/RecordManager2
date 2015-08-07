package cz.mzk.recordmanager.server.dedup;

public class TitleForDeduplication {
	
	private Long id;
	
	private Long harvestedRecordId;
	
	private String title;
	
	private String isbn;
	
	private String cnb;
	
	private String authorStr;

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getCnb() {
		return cnb;
	}

	public void setCnb(String cnb) {
		this.cnb = cnb;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getHarvestedRecordId() {
		return harvestedRecordId;
	}

	public void setHarvestedRecordId(Long harvestedRecordId) {
		this.harvestedRecordId = harvestedRecordId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthorStr() {
		return authorStr;
	}

	public void setAuthorStr(String authorStr) {
		this.authorStr = authorStr;
	}

	@Override
	public String toString() {
		return "TitleForDeduplication [id=" + id + ", harvestedRecordId="
				+ harvestedRecordId + ", title=" + title + "]";
	}
	
	
}
