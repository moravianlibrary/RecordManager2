package cz.mzk.recordmanager.api.model.configurations;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;

import java.io.Serializable;

public class KrameriusConfigurationDto extends ImportConfigurationDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String url;

	private String urlSolr;

	private Long queryRows;

	private String metadataStream;

	private String authToken;

	private boolean downloadPrivateFulltexts;

	private String fulltextHarvestType = "fedora";

	private String harvestJobName;

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

	public String getHarvestJobName() {
		return harvestJobName;
	}

	public void setHarvestJobName(String harvestJobName) {
		this.harvestJobName = harvestJobName;
	}
}
