package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = KramAvailability.TABLE_NAME)
public class KramAvailability {

	public static final String TABLE_NAME = "kram_availability";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "import_conf_id", referencedColumnName = "import_conf_id", nullable = false)
	private KrameriusConfiguration harvestedFrom;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "availability")
	private String availability;

	@Column(name = "dnnt")
	private boolean dnnt;

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	public String getLink() {
		return harvestedFrom.getAvailabilityDestUrl() + uuid;
	}

	public String getDnntLink() {
		return harvestedFrom.getDnntDestUrl() + uuid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public KrameriusConfiguration getHarvestedFrom() {
		return harvestedFrom;
	}

	public void setHarvestedFrom(KrameriusConfiguration harvestedFrom) {
		this.harvestedFrom = harvestedFrom;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Date getLastHarvest() {
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}

	public boolean isDnnt() {
		return dnnt;
	}

	public void setDnnt(boolean dnnt) {
		this.dnnt = dnnt;
	}

	@Override
	public String toString() {
		return "KramAvailability{" +
				"id=" + id +
				", harvestedFrom=" + harvestedFrom +
				", uuid='" + uuid + '\'' +
				", availability='" + availability + '\'' +
				", dnnt=" + dnnt +
				", updated=" + updated +
				", lastHarvest=" + lastHarvest +
				'}';
	}
}