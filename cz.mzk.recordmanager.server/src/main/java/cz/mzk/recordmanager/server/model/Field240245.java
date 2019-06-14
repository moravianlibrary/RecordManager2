package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = Field240245.TABLE_NAME)
public class Field240245 extends AbstractDomainObject {

	public static final String TABLE_NAME = "field240245";

	@Column(name = "title")
	private String title = "";

	public static Field240245 create(final String value) {
		Field240245 newTitle = new Field240245();
		newTitle.setField240245Str(value);
		return newTitle;
	}

	public String getField240245Str() {
		return title;
	}

	public void setField240245Str(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "Title [title=" + title + ']';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Field240245 blTitle = (Field240245) o;

		return title.equals(blTitle.title);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + title.hashCode();
		return result;
	}
}
