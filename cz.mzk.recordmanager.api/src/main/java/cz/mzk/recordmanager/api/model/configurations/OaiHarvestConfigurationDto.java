package cz.mzk.recordmanager.api.model.configurations;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;

import java.io.Serializable;

public class OaiHarvestConfigurationDto extends ImportConfigurationDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String url;

	private String extractIdRegex;

	private String harvestJobName;

	private String metadataPrefix;

	private String set;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMetadataPrefix() {
		return metadataPrefix;
	}

	public void setMetadataPrefix(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}

	public String getExtractIdRegex() {
		return extractIdRegex;
	}

	public void setExtractIdRegex(String extractIdRegex) {
		this.extractIdRegex = extractIdRegex;
	}

	public String getHarvestJobName() {
		return harvestJobName;
	}

	public void setHarvestJobName(String harvestJobName) {
		this.harvestJobName = harvestJobName;
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}

}
