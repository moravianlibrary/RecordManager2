package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = Uuid.TABLE_NAME)
public class Uuid extends AbstractDomainObject {

	public static final String TABLE_NAME = "uuid";

	@Column(name = "harvested_record_id", updatable = false, insertable = false)
	private Long harvestedRecordId;

	@Column(name = "uuid")
	private String uuid;

	public static Uuid create(final String uuid) {
		Uuid newUuid = new Uuid();
		newUuid.setUuid(uuid);
		return newUuid;
	}

	public Long getHarvestedRecordId() {
		return harvestedRecordId;
	}

	public void setHarvestedRecordId(Long harvestedRecordId) {
		this.harvestedRecordId = harvestedRecordId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Uuid uuid1 = (Uuid) o;
		return uuid.equals(uuid1.uuid);
	}

	@Override
	public int hashCode() {
		return 1;
	}
}
