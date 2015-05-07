package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Cnb.TABLE_NAME)
public class Cnb extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "cnb";
	
	@Column(name="cnb")
	private String cnb;

	public String getCnb() {
		return cnb;
	}

	public void setCnb(String cnb) {
		this.cnb = cnb;
	}

	@Override
	public String toString() {
		return "Cnb [cnb=" + cnb + "]";
	}
}
