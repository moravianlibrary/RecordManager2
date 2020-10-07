package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Date;

@Entity
@Table(name=ObalkyKnihTOC.TABLE_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "book")
public class ObalkyKnihTOC extends AbstractDomainObject {

	public static final String TABLE_NAME = "obalkyknih_toc";

	@Embeddable
	private static class BibInfo {

		@Column(name = "nbn")
		@XmlElement(name = "cnb")
		public String nbn;

		@Column(name = "oclc")
		@XmlElement(name = "oclc")
		public String oclc;

		@XmlElement(name = "ean13")
		@Transient
		public String eanStr;

		@Column(name = "isbn")
		@XmlTransient
		public Long isbn;

		public String ean;

		@Override
		public String toString() {
			return "BibInfo{" +
					"nbn='" + nbn + '\'' +
					", oclc='" + oclc + '\'' +
					", isbn=" + isbn +
					", ean='" + ean + '\'' +
					'}';
		}

	}

	@Column(name="book_id")
	@XmlElement(name="book_id")
	private Long bookId;

	@Embedded
	@XmlElement(name="bibinfo")
	private BibInfo bibInfo = new BibInfo();

	@XmlElement(name = "book_metadata_change")
	@Transient
	private String updatedStr;

	@Column(name = "updated")
	@XmlTransient
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Column(name = "last_harvest")
	@XmlTransient
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	@Column(name="toc")
	@XmlElement(name="toc")
	private String toc;


	public Long getBookId() {
		return bookId;
	}

	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}

	public String getNbn() {
		return bibInfo.nbn;
	}

	public void setNbn(String nbn) {
		this.bibInfo.nbn = nbn;
	}

	public String getOclc() {
		return bibInfo.oclc;
	}

	public void setOclc(String oclc) {
		this.bibInfo.oclc = oclc;
	}

	public String getEan() {
		return bibInfo.ean;
	}

	public void setEan(String ean) {
		this.bibInfo.ean = ean;
	}

	public Long getIsbn() {
		return bibInfo.isbn;
	}

	public void setIsbn(Long isbn) {
		this.bibInfo.isbn = isbn;
	}

	public String getEanStr() {
		return bibInfo.eanStr;
	}

	public void setEanStr(String eanStr) {
		this.bibInfo.eanStr = eanStr;
	}

	public String getToc() {
		return toc;
	}

	public void setToc(String toc) {
		this.toc = toc;
	}

	public String getUpdatedStr() {
		return updatedStr;
	}

	public void setUpdatedStr(String updatedStr) {
		this.updatedStr = updatedStr;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Date getLastHarvest() {
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}

	public BibInfo getBibInfo() {
		return bibInfo;
	}

	public void setBibInfo() {
		this.bibInfo = new BibInfo();
	}

	@Override
	public String toString() {
		return "ObalkyKnihTOC{" +
				"bookId=" + bookId +
				", bibInfo=" + bibInfo +
				'}';
	}

}
