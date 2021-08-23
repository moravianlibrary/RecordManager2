package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = SiglaCaslin.TABLE_NAME)
public class SiglaCaslin {

	public static final String TABLE_NAME = "sigla_caslin";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sigla")
	private String sigla;

	@Column(name = "url")
	private String url;

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	public static SiglaCaslin create(String sigla, String url) {
		SiglaCaslin newSigla = new SiglaCaslin();
		newSigla.setSigla(sigla);
		newSigla.setUrl(url);
		newSigla.setUpdated(new Date());
		newSigla.setLastHarvest(new Date());
		return newSigla;
	}

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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