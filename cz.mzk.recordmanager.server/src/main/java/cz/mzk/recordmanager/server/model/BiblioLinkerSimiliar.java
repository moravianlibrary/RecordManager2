package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BiblioLinkerSimiliar.TABLE_NAME)
public class BiblioLinkerSimiliar extends AbstractDomainObject {

	public static final String TABLE_NAME = "biblio_linker_similar";

	@Column(name = "url_id")
	private String urlId;

	public static BiblioLinkerSimiliar create(final String id) {
		BiblioLinkerSimiliar newBLSimilar = new BiblioLinkerSimiliar();
		newBLSimilar.setUrlId(id);
		return newBLSimilar;
	}

	public void setUrlId(String urlId) {
		this.urlId = urlId;
	}

	public String getUrlId() {
		return urlId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BiblioLinkerSimiliar that = (BiblioLinkerSimiliar) o;

		return urlId != null ? urlId.equals(that.urlId) : that.urlId == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (urlId != null ? urlId.hashCode() : 0);
		return result;
	}
}
