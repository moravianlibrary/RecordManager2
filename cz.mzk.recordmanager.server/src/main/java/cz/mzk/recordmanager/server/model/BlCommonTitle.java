package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BlCommonTitle.TABLE_NAME)
public class BlCommonTitle extends AbstractDomainObject {

	public static final String TABLE_NAME = "bl_common_title";

	@Column(name = "title")
	private String title = "";

	public static BlCommonTitle create(final String value) {
		BlCommonTitle newTitle = new BlCommonTitle();
		newTitle.setBlCommonTitleStr(value);
		return newTitle;
	}

	public String getBlCommonTitleStr() {
		return title;
	}

	public void setBlCommonTitleStr(String title) {
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

		BlCommonTitle blTitle = (BlCommonTitle) o;

		return title.equals(blTitle.title);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + title.hashCode();
		return result;
	}
}
