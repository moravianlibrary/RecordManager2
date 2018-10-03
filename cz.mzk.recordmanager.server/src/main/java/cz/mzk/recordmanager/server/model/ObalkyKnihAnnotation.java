package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Date;

@Entity
@Table(name = ObalkyKnihAnnotation.TABLE_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "book")
public class ObalkyKnihAnnotation extends AbstractDomainObject {

	public static final String TABLE_NAME = "obalkyknih_annotation";

	@Embeddable
	private static class BibInfo {

		@Column(name = "cnb")
		@XmlElement(name = "cnb")
		public String cnb;

		@Column(name = "oclc")
		@XmlElement(name = "oclc")
		public String oclc;

		@XmlElement(name = "ean13")
		@Transient
		public String isbnStr;

		@Column(name = "isbn")
		@XmlTransient
		public Long isbn;

		@Override
		public String toString() {
			return "BibInfo{" +
					"nbn='" + cnb + '\'' +
					", oclc='" + oclc + '\'' +
					", isbn='" + isbn + '\'' +
					'}';
		}
	}

	@Column(name = "book_id")
	@XmlElement(name = "book_id")
	private Long bookId;

	@Embedded
	@XmlElement(name = "bibinfo")
	private ObalkyKnihAnnotation.BibInfo bibInfo = new ObalkyKnihAnnotation.BibInfo();

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

	@Column(name = "annotation")
	@XmlElement(name = "annotation")
	private String annotation;

	public String getCnb() {
		return bibInfo.cnb;
	}

	public void setCnb(String cnb) {
		this.bibInfo.cnb = cnb;
	}

	public String getOclc() {
		return bibInfo.oclc;
	}

	public void setOclc(String oclc) {
		this.bibInfo.oclc = oclc;
	}

	public Long getIsbn() {
		return bibInfo.isbn;
	}

	public void setIsbn(Long isbn) {
		this.bibInfo.isbn = isbn;
	}

	public String getIsbnStr() {
		return bibInfo.isbnStr;
	}

	public void setIsbn(String isbnStr) {
		this.bibInfo.isbnStr = isbnStr;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getUpdatedStr() {
		return updatedStr;
	}

	public Date getLastHarvest() {
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}

	public Long getBookId() {
		return bookId;
	}

	@Override
	public String toString() {
		return "ObalkyKnihAnnotation{" +
				"bookId=" + bookId +
				", bibInfo=" + bibInfo +
				", updatedStr='" + updatedStr + '\'' +
				", updated=" + updated +
				", lastHarvest=" + lastHarvest +
				", annotation='" + annotation + '\'' +
				'}';
	}
}
