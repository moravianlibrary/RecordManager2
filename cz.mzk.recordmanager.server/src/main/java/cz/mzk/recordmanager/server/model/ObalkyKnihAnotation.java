package cz.mzk.recordmanager.server.model;

import javax.persistence.*;

@Entity
@Table(name = ObalkyKnihAnotation.TABLE_NAME)
public class ObalkyKnihAnotation extends AbstractDomainObject {

	public static final String TABLE_NAME = "obalkyknih_anotation";

	@Embeddable
	private static class BibInfo {

		@Column(name = "nbn")
		public String nbn;

		@Column(name = "oclc")
		public String oclc;

		@Column(name = "isbn")
		public String isbn;
	}

	@Embedded
	private BibInfo bibInfo = new BibInfo();

	@Column(name = "anotation")
	private String anotation;

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

	public String getIsbn() {
		return bibInfo.isbn;
	}

	public void setIsbn(String isbn) {
		this.bibInfo.isbn = isbn;
	}

	public String getAnotation() {
		return anotation;
	}

	public void setAnotation(String anotation) {
		this.anotation = anotation;
	}
}
