package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = Loc.TABLE_NAME)
public class Loc extends AbstractDomainObject {

	public static final String TABLE_NAME = "loc";

	@Column(name = "harvested_record_id", updatable = false, insertable = false)
	private Long harvestedRecordId;

	@Column(name = "loc")
	private String loc;

	public static Loc create(final String loc) {
		Loc newLoc = new Loc();
		newLoc.setLoc(loc);
		return newLoc;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Loc)) return false;
		if (!super.equals(o)) return false;
		Loc loc1 = (Loc) o;
		return harvestedRecordId.equals(loc1.harvestedRecordId) && loc.equals(loc1.loc);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), harvestedRecordId, loc);
	}

	@Override
	public String toString() {
		return "Loc{" +
				"harvestedRecordId=" + harvestedRecordId +
				", loc='" + loc + '\'' +
				'}';
	}

}
