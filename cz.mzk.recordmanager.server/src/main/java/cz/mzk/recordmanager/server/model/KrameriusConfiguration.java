package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = KrameriusConfiguration.TABLE_NAME)
@PrimaryKeyJoinColumn(name="import_conf_id")
public class KrameriusConfiguration extends ImportConfiguration {
	
	public static final String TABLE_NAME = "kramerius_conf";
	
	@Column(name="url")
	private String url;
	
	@Column(name="url_solr")
	private String urlSolr;
	
	@Column(name="query_rows")
	private Long queryRows;
	
	@Column(name="metadata_stream")
	private String metadataStream;

	@Column(name="auth_token")
	private String authToken;
	
	@Column(name="download_private_fulltexts")
	private boolean downloadPrivateFulltexts;

	@Column(name="fulltext_harvest_type")
	private String fulltextHarvestType = "fedora";

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlSolr() {
		return urlSolr;
	}

	public void setUrlSolr(String urlSolr) {
		this.urlSolr = urlSolr;
	}

	public Long getQueryRows() {
		return queryRows;
	}

	public void setQueryRows(Long queryRows) {
		this.queryRows = queryRows;
	}

	public String getMetadataStream() {
		return metadataStream;
	}

	public void setMetadataStream(String metadataStream) {
		this.metadataStream = metadataStream;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public boolean isDownloadPrivateFulltexts() {
		return downloadPrivateFulltexts;
	}

	public void setDownloadPrivateFulltexts(boolean downloadPrivateFulltexts) {
		this.downloadPrivateFulltexts = downloadPrivateFulltexts;
	}

	public String getFulltextHarvestType() {
		return fulltextHarvestType;
	}

	public void setFulltextHarvestType(String fulltextHarvestType) {
		this.fulltextHarvestType = fulltextHarvestType;
	}

}
