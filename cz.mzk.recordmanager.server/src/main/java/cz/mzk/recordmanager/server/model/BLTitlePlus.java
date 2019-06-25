package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BLTitlePlus.TABLE_NAME)
public class BLTitlePlus extends AbstractDomainObject {

	public static final String TABLE_NAME = "bl_title_plus";

	@Column(name = "title_plus")
	private String titlePlus = "";

	public static BLTitlePlus create(final String value) {
		BLTitlePlus newTitlePLus = new BLTitlePlus();
		newTitlePLus.setBLTitlePlusStr(value);
		return newTitlePLus;
	}

	public String getBLTitlePlusStr() {
		return titlePlus;
	}

	public void setBLTitlePlusStr(String title_plus) {
		this.titlePlus = title_plus;
	}

	@Override
	public String toString() {
		return "Title plus [titlePlus=" + titlePlus + ']';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BLTitlePlus blTitlePlus = (BLTitlePlus) o;

		return titlePlus.equals(blTitlePlus.titlePlus);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + titlePlus.hashCode();
		return result;
	}
}
