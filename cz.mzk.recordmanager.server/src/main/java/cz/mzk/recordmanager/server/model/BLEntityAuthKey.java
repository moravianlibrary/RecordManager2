package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BLEntityAuthKey.TABLE_NAME)
public class BLEntityAuthKey extends AbstractDomainObject {

	public static final String TABLE_NAME = "bl_entity_auth_key";

	@Column(name = "entity_auth_key")
	private String entityAuthKey = "";

	public static BLEntityAuthKey create(final String value) {
		BLEntityAuthKey newEntityAuthKey = new BLEntityAuthKey();
		newEntityAuthKey.setBLEntityAuthKeyStr(value);
		return newEntityAuthKey;
	}

	public String getBLEntityAuthKeyStr() {
		return entityAuthKey;
	}

	public void setBLEntityAuthKeyStr(String entity) {
		this.entityAuthKey = MetadataUtils.normalizeAndShorten(entity, 50);
	}

	@Override
	public String toString() {
		return "EntityAuthKey [entityAuthKey=" + entityAuthKey + ']';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BLEntityAuthKey blEntityAuthKey = (BLEntityAuthKey) o;

		return entityAuthKey.equals(blEntityAuthKey.entityAuthKey);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + entityAuthKey.hashCode();
		return result;
	}
}
