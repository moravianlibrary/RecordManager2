package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ZiskejLibrary.TABLE_NAME)
public class ZiskejLibrary {

	public static final String TABLE_NAME = "ziskej_library";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sigla")
	private String sigla;

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
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
}