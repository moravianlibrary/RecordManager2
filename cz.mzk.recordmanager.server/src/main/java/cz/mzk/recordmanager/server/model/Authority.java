package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = Authority.TABLE_NAME)
public class Authority extends AbstractDomainObject {

	public static final String TABLE_NAME = "authority";

	@Column(name = "authority_id")
	private String authorityId;

	public static Authority create(final String id) {
		Authority newAuthority = new Authority();
		newAuthority.setAuthorityId(id);
		return newAuthority;
	}

	public String getAuthorityId() {
		return authorityId;
	}

	public void setAuthorityId(String authorityId) {
		this.authorityId = authorityId;
	}

}
