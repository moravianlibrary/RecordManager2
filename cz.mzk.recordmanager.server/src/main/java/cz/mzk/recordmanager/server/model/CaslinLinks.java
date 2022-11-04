package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = CaslinLinks.TABLE_NAME)
public class CaslinLinks {

	public static final String TABLE_NAME = "caslin_links";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sigla")
	private String sigla;

	@Column(name = "url")
	private String url;

	@Column(name = "hardcoded_url")
	private String hardcodedUrl;

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest = new Date();

	public static CaslinLinks create(String sigla, String url) {
		CaslinLinks newCaslinLink = new CaslinLinks();
		newCaslinLink.setSigla(sigla);
		newCaslinLink.setUrl(url);
		newCaslinLink.setUpdated(new Date());
		newCaslinLink.setLastHarvest(new Date());
		return newCaslinLink;
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

	public String getHardcodedUrl() {
		return hardcodedUrl;
	}

	public void setHardcodedUrl(String hardcodedUrl) {
		this.hardcodedUrl = hardcodedUrl;
	}

	public String getUrlForIndexing() {
		return hardcodedUrl != null ? hardcodedUrl : url;
	}

}