package cz.mzk.recordmanager.server.model;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BiblioLinkerSimiliar.TABLE_NAME)
public class BiblioLinkerSimiliar extends AbstractDomainObject implements Comparable {

	public static final String TABLE_NAME = "biblio_linker_similar";

	@Column(name = "url_id")
	private String urlId;

	@Type(
			type = "cz.mzk.recordmanager.server.hibernate.StringEnumUserType",
			parameters = {
					@Parameter(name = "enumClassName", value = "cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType"),
			}
	)
	@Column(name = "type")
	private BiblioLinkerSimilarType type;

	public static BiblioLinkerSimiliar create(final String id, final BiblioLinkerSimilarType type) {
		BiblioLinkerSimiliar newBLSimilar = new BiblioLinkerSimiliar();
		newBLSimilar.setUrlId(id);
		newBLSimilar.setType(type);
		return newBLSimilar;
	}

	public void setUrlId(String urlId) {
		this.urlId = urlId;
	}

	public String getUrlId() {
		return urlId;
	}

	public BiblioLinkerSimilarType getType() {
		return type;
	}

	public void setType(BiblioLinkerSimilarType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BiblioLinkerSimiliar that = (BiblioLinkerSimiliar) o;

		if (!urlId.equals(that.urlId)) return false;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + urlId.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "BiblioLinkerSimiliar{" +
				"urlId='" + urlId + '\'' +
				", type=" + type +
				'}';
	}

	@Override
	public int compareTo(Object o) {
		if (this == o || o == null || this.getClass() != o.getClass()) return 0;
		BiblioLinkerSimiliar other = (BiblioLinkerSimiliar) o;
		return urlId.compareTo(other.getUrlId());
	}
}
