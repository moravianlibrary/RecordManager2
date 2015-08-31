package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Language.TABLE_NAME)
public class Language extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "language";
	
	@Column(name="lang")
	private String langStr;

	public String getLangStr() {
		return langStr;
	}

	public void setLangStr(String langStr) {
		this.langStr = langStr;
	}

	@Override
	public String toString() {
		return "Language [langStr=" + langStr + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((langStr == null) ? 0 : langStr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Language other = (Language) obj;
		if (langStr == null) {
			if (other.langStr != null)
				return false;
		} else if (!langStr.equals(other.langStr))
			return false;
		return true;
	}
	
	

}
