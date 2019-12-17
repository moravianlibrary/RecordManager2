package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BLEntity.TABLE_NAME)
public class BLEntity extends AbstractDomainObject {

	public static final String TABLE_NAME = "bl_entity";

	@Column(name = "entity")
	private String entity = "";

	public static BLEntity create(final String value) {
		BLEntity newEntity = new BLEntity();
		newEntity.setBLEntityStr(value);
		return newEntity;
	}

	public String getBLEntityStr() {
		return entity;
	}

	public void setBLEntityStr(String entity) {
		this.entity = MetadataUtils.normalizeAndShorten(entity, 200);
	}

	@Override
	public String toString() {
		return "Entity [entity=" + entity + ']';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BLEntity blEntity = (BLEntity) o;

		return entity.equals(blEntity.entity);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + entity.hashCode();
		return result;
	}
}
