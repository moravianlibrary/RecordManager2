package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BLTitle.TABLE_NAME)
public class BLTitle extends AbstractDomainObject {

	public static final String TABLE_NAME = "bl_title";

	@Column(name = "title")
	private String title = "";

	public static BLTitle create(final String value) {
		BLTitle newTitle = new BLTitle();
		newTitle.setBLTitleStr(value);
		return newTitle;
	}

	public String getBLTitleStr() {
		return title;
	}

	public void setBLTitleStr(String title) {
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

		BLTitle blTitle = (BLTitle) o;

		return title.equals(blTitle.title);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + title.hashCode();
		return result;
	}
}
