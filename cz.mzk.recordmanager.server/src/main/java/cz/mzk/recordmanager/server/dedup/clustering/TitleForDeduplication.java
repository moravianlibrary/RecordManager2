package cz.mzk.recordmanager.server.dedup.clustering;

import cz.mzk.recordmanager.server.util.DeduplicationUtils;

public class TitleForDeduplication extends TitleClusterable {
	
	private Long harvestedRecordId;
	
	private String isbn;
	
	private String cnb;
	
	private String authorStr;
	
	private Long pages;

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

	public Long getHarvestedRecordId() {
		return harvestedRecordId;
	}

	public void setHarvestedRecordId(Long harvestedRecordId) {
		this.harvestedRecordId = harvestedRecordId;
	}

	public String getAuthorStr() {
		return authorStr;
	}

	public void setAuthorStr(String authorStr) {
		this.authorStr = authorStr;
	}

	public Long getPages() {
		return pages;
	}

	public void setPages(Long pages) {
		this.pages = pages;
	}

	@Override
	public String toString() {
		return "TitleForDeduplication [id=" + this.getId() + ", harvestedRecordId="
				+ harvestedRecordId + ", title=" + this.getTitle() + ", isbn=" + isbn
				+ ", cnb=" + cnb + ", authorStr=" + authorStr + ", pages="
				+ pages + "]";
	}

	@Override
	public int computeSimilarityPercentage(Clusterable other) {
		if (other == null || !(other instanceof TitleForDeduplication)) {
			return 0;
		}
		TitleForDeduplication otherTitle = (TitleForDeduplication) other;
		
		if (this.getTitle().equals(otherTitle.getTitle())) {
			// ignore same titles, these are deduplicated in previous
			// steps
			return 0;
		}
		if (this.getIsbn() != null
				&& otherTitle.getIsbn() != null
				&& !this.getIsbn().equals(otherTitle.getIsbn())) {
			// ignore different ISBNs
			return 0;
		}
		if (this.getCnb() != null && otherTitle.getCnb() != null
				&& !this.getCnb().equals(otherTitle.getCnb())) {
			// ignore different CNBs
			return 0;
		}
		if (this.getAuthorStr() != null
				&& otherTitle.getAuthorStr() != null
				&& !this.getAuthorStr().equals(
						otherTitle.getAuthorStr())) {
			// ignore different authors
			return 0;
		}

		if (!DeduplicationUtils.comparePages(this.getPages(), otherTitle.getPages(), .05, 10)) {
			// ignore different pages
			return 0;
		} 

		return super.computeSimilarityPercentage(otherTitle);
	}
	
}
