package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = KramAvailability.TABLE_NAME)
public class KramAvailability {

	public static final String TABLE_NAME = "kram_availability";

	private static final Logger logger = LoggerFactory.getLogger(KramAvailability.class);

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "import_conf_id", referencedColumnName = "import_conf_id", nullable = false)
	private KrameriusConfiguration harvestedFrom;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "parent_uuid")
	private String parentUuid;

	@Column(name = "availability")
	private String availability;

	@Column(name = "dnnt")
	private boolean dnnt;

	@Column(name = "level")
	private Integer level;

	@Column(name = "issn")
	private String issn;

	@Column(name = "publication_year")
	private Integer yaer;

	@Column(name = "volume")
	private String volume;

	@Column(name = "issue")
	private Integer issue;

	@Column(name = "page")
	private Integer page;

	@Column(name = "type")
	private String type;

	@Column(name = "rels_ext_index")
	private Integer relsExtIndex;

	@Column(name = "dedup_key")
	private String dedupKey;

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "kram_availability_id", referencedColumnName = "id", nullable = false)
	private List<KramDnntLabel> labels = new ArrayList<>();

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
		if (uuid.length() >= 100) {
			logger.warn(String.format("too long uuid: %s", uuid));
			uuid = MetadataUtils.shorten(uuid, 100);
		}
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

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public List<KramDnntLabel> getDnntLabels() {
		return labels;
	}

	public void setDnntLabels(List<KramDnntLabel> labels) {
		this.labels = labels;
	}

	public void addDnntLabel(String labelName) {
		if (this.labels == null) this.labels = new ArrayList<>();
		KramDnntLabel newLabel = KramDnntLabel.create(labelName);
		if (newLabel == null || this.labels.contains(newLabel)) return;
		this.labels.add(newLabel);
	}

	public String getParentUuid() {
		return parentUuid;
	}

	public void setParentUuid(String parentUuid) {
		if (parentUuid != null && parentUuid.length() >= 100) {
			logger.warn(String.format("uuid: %s, too long parentUuid: %s", this.uuid, parentUuid));
			parentUuid = MetadataUtils.shorten(parentUuid, 100);
		}
		this.parentUuid = parentUuid;
	}

	public String getIssn() {
		return issn;
	}

	public void setIssn(String issn) {
		if (issn != null && issn.length() >= 20) {
			logger.warn(String.format("uuid: %s, too long issn: %s", this.uuid, issn));
			issn = MetadataUtils.shorten(issn, 20);
		}
		this.issn = issn;
	}

	public Integer getYaer() {
		return yaer;
	}

	public void setYaer(Integer yaer) {
		this.yaer = yaer;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		if (volume != null && volume.length() >= 20) {
			logger.warn(String.format("uuid: %s, too long volume: %s", this.uuid, volume));
			volume = MetadataUtils.shorten(volume, 20);
		}
		this.volume = volume;
	}

	public Integer getIssue() {
		return issue;
	}

	public void setIssue(Integer issue) {
		this.issue = issue;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public String getType() {
		return type;
	}

	public String getDedupKey() {
		return dedupKey;
	}

	public Integer getRelsExtIndex() {
		return relsExtIndex;
	}

	public void setRelsExtIndex(Integer relsExtIndex) {
		this.relsExtIndex = relsExtIndex;
	}

	public void setDedupKey(String dedupKey) {
		if (dedupKey != null && dedupKey.length() >= 100) {
			logger.warn(String.format("uuid: %s, too long dedupKey: %s", this.uuid, dedupKey));
			dedupKey = MetadataUtils.shorten(dedupKey, 100);
		}
		this.dedupKey = dedupKey;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "KramAvailability{" +
				"id=" + id +
				", harvestedFrom=" + harvestedFrom +
				", uuid='" + uuid + '\'' +
				", parentUuid='" + parentUuid + '\'' +
				", availability='" + availability + '\'' +
				", dnnt=" + dnnt +
				", level=" + level +
				", issn='" + issn + '\'' +
				", yaer=" + yaer +
				", volume=" + volume +
				", issue=" + issue +
				", page=" + page +
				", type='" + type + '\'' +
				", relsExtIndex=" + relsExtIndex +
				", dedupKey='" + dedupKey + '\'' +
				", updated=" + updated +
				", lastHarvest=" + lastHarvest +
				", labels=" + labels +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KramAvailability that = (KramAvailability) o;
		return dnnt == that.dnnt
				&& Objects.equals(parentUuid, that.parentUuid)
				&& Objects.equals(availability, that.availability)
				&& Objects.equals(issn, that.issn)
				&& Objects.equals(yaer, that.yaer)
				&& Objects.equals(volume, that.volume)
				&& Objects.equals(issue, that.issue)
				&& Objects.equals(page, that.page)
				&& Objects.equals(relsExtIndex, that.relsExtIndex)
				&& Objects.equals(type, that.type)
				&& CollectionUtils.isEqualCollection(labels, that.labels);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parentUuid, availability, dnnt, issn, yaer, volume, issue, page, relsExtIndex, type, labels);
	}

}