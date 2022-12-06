package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = HarvestedRecordInspiration.TABLE_NAME)
public class HarvestedRecordInspiration {

	public static final String TABLE_NAME = "harvested_record_inspiration";

	public HarvestedRecordInspiration() {
	}

	public static HarvestedRecordInspiration create(HarvestedRecord hr, Inspiration inspiration) {
		HarvestedRecordInspiration newHRInspiration = new HarvestedRecordInspiration();
		newHRInspiration.setHarvestedRecordId(hr.getId());
		newHRInspiration.setInspirationId(inspiration.getId());
		newHRInspiration.setUpdated(new Date());
		newHRInspiration.setLastHarvest(new Date());
		return newHRInspiration;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "harvested_record_id")
	private Long harvestedRecordId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "harvested_record_id", nullable = false, updatable = false, insertable = false)
	private HarvestedRecord harvestedRecord;

	@Column(name = "inspiration_id")
	private Long inspirationId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "inspiration_id", nullable = false, updatable = false, insertable = false)
	private Inspiration inspiration;

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

	public Long getInspirationId() {
		return inspirationId;
	}

	public void setInspirationId(Long inspirationId) {
		this.inspirationId = inspirationId;
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

	public Inspiration getInspiration() {
		return inspiration;
	}

	public void setInspiration(Inspiration inspiration) {
		this.inspiration = inspiration;
	}

	public HarvestedRecord getHarvestedRecord() {
		return harvestedRecord;
	}

	public void setHarvestedRecord(HarvestedRecord harvestedRecord) {
		this.harvestedRecord = harvestedRecord;
	}

	@Override
	public String toString() {
		return "Inspiration [harvestedRecordId=" + harvestedRecordId + "]";
	}

}
