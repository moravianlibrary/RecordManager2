package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = Inspiration.TABLE_NAME)
public class Inspiration {

	public static final String TABLE_NAME = "inspiration";

	public Inspiration() {
	}

	public static Inspiration create(HarvestedRecord hr, InspirationName inspirationName) {
		Inspiration newInspiration = new Inspiration();
		newInspiration.setHarvestedRecordId(hr.getId());
		newInspiration.setInspirationNameId(inspirationName.getId());
		newInspiration.setUpdated(new Date());
		newInspiration.setLastHarvest(new Date());
		return newInspiration;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "harvested_record_id")
	private Long harvestedRecordId;

	@Column(name = "inspiration_name_id")
	private Long inspirationNameId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "inspiration_name_id", nullable = false, updatable = false, insertable = false)
	private InspirationName inspirationName;

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	public Long getHarvestedRecordId() {
		return harvestedRecordId;
	}

	public void setHarvestedRecordId(Long id) {
		this.harvestedRecordId = id;
	}

	public Long getInspirationNameId() {
		return inspirationNameId;
	}

	public void setInspirationNameId(Long inspirationNameId) {
		this.inspirationNameId = inspirationNameId;
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

	public InspirationName getInspirationName() {
		return inspirationName;
	}

	public void setInspirationName(InspirationName inspirationName) {
		this.inspirationName = inspirationName;
	}

	@Override
	public String toString() {
		return "Inspiration [harvestedRecordId=" + harvestedRecordId + "]";
	}

}
