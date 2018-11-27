package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name= BiblioLinker.TABLE_NAME)
public class BiblioLinker extends AbstractDomainObject {

//	TODO consider usage of database sequence in id
	public static final String TABLE_NAME = "biblio_linker";

	@Column(name="updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated = new Date();

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Override
	public String toString() {
		return String.format("BiblioLinker[id=%s]", getId());
	}

}
