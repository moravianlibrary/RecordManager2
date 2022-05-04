package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = Loc.TABLE_NAME)
public class Loc extends AbstractDomainObject {

	public static final String TABLE_NAME = "loc";
	private final static int EFFECTIVE_LENGTH_20 = 20;

	@Column(name = "harvested_record_id", updatable = false, insertable = false)
	private Long harvestedRecordId;

	@Column(name = "loc")
	private String loc;

	@Column(name = "subfield")
	private String subfield;

	public static Loc create(final String loc, final char subfield) {
		Loc newLoc = new Loc();
		newLoc.setLoc(MetadataUtils.shorten(loc, EFFECTIVE_LENGTH_20));
		newLoc.setSubfield(String.valueOf(subfield));
		return newLoc;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	public String getSubfield() {
		return subfield;
	}

	public void setSubfield(String subfield) {
		this.subfield = subfield;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Loc)) return false;
		if (!super.equals(o)) return false;
		Loc loc1 = (Loc) o;
		return harvestedRecordId.equals(loc1.harvestedRecordId) && loc.equals(loc1.loc) && Objects.equals(subfield, loc1.subfield);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), harvestedRecordId, loc, subfield);
	}

	@Override
	public String toString() {
		return "Loc{" +
				"harvestedRecordId=" + harvestedRecordId +
				", loc='" + loc + '\'' +
				", subfield='" + subfield + '\'' +
				'}';
	}

}
