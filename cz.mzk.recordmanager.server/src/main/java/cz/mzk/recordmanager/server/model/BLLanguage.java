package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BLLanguage.TABLE_NAME)
public class BLLanguage extends AbstractDomainObject {

	public static final String TABLE_NAME = "bl_language";

	@Column(name = "lang")
	private String lang = "";

	public static BLLanguage create(final String value) {
		BLLanguage blLanguage = new BLLanguage();
		blLanguage.setBLLanguageStr(value);
		return blLanguage;
	}

	public String getBLLanguageStr() {
		return lang;
	}

	public void setBLLanguageStr(String lang) {
		this.lang = lang;
	}

	@Override
	public String toString() {
		return "Language [lang=" + lang + ']';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BLLanguage blLanguage = (BLLanguage) o;

		return lang.equals(blLanguage.lang);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + lang.hashCode();
		return result;
	}

}
