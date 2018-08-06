package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = Authority.TABLE_NAME)
public class Authority extends AbstractDomainObject {

	public static final String TABLE_NAME = "authority";
	private static final int EFFECTIVE_LENGTH_20 = 20;

	@Column(name = "harvested_record_id")
	private Long harvestedRecordId;

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
		this.authorityId = MetadataUtils.shorten(authorityId, EFFECTIVE_LENGTH_20);
	}

}
