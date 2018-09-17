package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ObalkyKnihAnotation.TABLE_NAME)
public class ObalkyKnihAnotation extends AbstractDomainObject {

	public static final String TABLE_NAME = "obalkyknih_anotation";

	private static final int EFFECTIVE_LENGHT = 32;

	@Embeddable
	private static class BibInfo {

		@Column(name = "nbn")
		public String nbn;

		@Column(name = "oclc")
		public String oclc;

		@Column(name = "isbn")
		public Long isbn;

		@Override
		public String toString() {
			return "BibInfo{" +
					"nbn='" + nbn + '\'' +
					", oclc='" + oclc + '\'' +
					", isbn='" + isbn + '\'' +
					'}';
		}
	}

	@Embedded
	private BibInfo bibInfo = new BibInfo();

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated = new Date();

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	@Column(name = "anotation")
	private String anotation;

	public String getNbn() {
		return bibInfo.nbn;
	}

	public void setNbn(String nbn) {
		if (nbn != null && nbn.length() > EFFECTIVE_LENGHT) {
			nbn = nbn.substring(0, EFFECTIVE_LENGHT);
		}
		this.bibInfo.nbn = nbn;
	}

	public String getOclc() {
		return bibInfo.oclc;
	}

	public void setOclc(String oclc) {
		if (oclc != null && oclc.length() > EFFECTIVE_LENGHT) {
			oclc = oclc.substring(0, EFFECTIVE_LENGHT);
		}
		this.bibInfo.oclc = oclc;
	}

	public Long getIsbn() {
		return bibInfo.isbn;
	}

	public void setIsbn(Long isbn) {
		this.bibInfo.isbn = isbn;
	}

	public String getAnotation() {
		return anotation;
	}

	public void setAnotation(String anotation) {
		this.anotation = anotation;
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

	@Override
	public String toString() {
		return "ObalkyKnihAnotation{" +
				"bibInfo=" + bibInfo +
				", updated=" + updated +
				", anotation='" + anotation + '\'' +
				'}';
	}
}
