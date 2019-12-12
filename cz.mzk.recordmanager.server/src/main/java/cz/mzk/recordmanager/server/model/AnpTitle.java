package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = AnpTitle.TABLE_NAME)
public class AnpTitle extends AbstractDomainObject {

	public static final String TABLE_NAME = "anp_title";

	@Column(name = "anp_title")
	private String anpTitle = "";

	@Column(name = "similarity_enabled")
	private boolean similarityEnabled;

	public static AnpTitle create(final String anpTitle, final boolean similarity) {
		AnpTitle newAnpTitle = new AnpTitle();
		newAnpTitle.setAnpTitle(anpTitle);
		newAnpTitle.setSimilarityEnabled(similarity);
		return newAnpTitle;
	}

	public String getAnpTitle() {
		return anpTitle;
	}

	public void setAnpTitle(String anpTitle) {
		this.anpTitle = anpTitle;
	}

	public boolean isSimilarityEnabled() {
		return similarityEnabled;
	}

	public void setSimilarityEnabled(boolean similarityEnabled) {
		this.similarityEnabled = similarityEnabled;
	}

	@Override
	public String toString() {
		return "AnpTitle{" +
				"anpTitle='" + anpTitle + '\'' +
				", similarityEnabled=" + similarityEnabled +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		AnpTitle anpTitle1 = (AnpTitle) o;

		if (similarityEnabled != anpTitle1.similarityEnabled) return false;
		return anpTitle != null ? anpTitle.equals(anpTitle1.anpTitle) : anpTitle1.anpTitle == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (anpTitle != null ? anpTitle.hashCode() : 0);
		result = 31 * result + (similarityEnabled ? 1 : 0);
		return result;
	}
}
