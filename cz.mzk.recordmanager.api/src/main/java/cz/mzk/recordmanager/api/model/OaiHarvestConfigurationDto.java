package cz.mzk.recordmanager.api.model;

import java.io.Serializable;

public class OaiHarvestConfigurationDto extends IdDto implements Serializable {

	private static final long serialVersionUID = 1L;


	private ContactPersonDto contact;

	private String idPrefix;

	private Long baseWeight;

	private boolean clusterIdEnabled;

	private boolean filteringEnabled;

	private boolean interceptionEnabled;

	private boolean isLibrary;

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

	
	public ContactPersonDto getContact() {
		return contact;
	}

	
	public void setContact(ContactPersonDto contact) {
		this.contact = contact;
	}

	
	public String getIdPrefix() {
		return idPrefix;
	}

	
	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	
	public Long getBaseWeight() {
		return baseWeight;
	}

	
	public void setBaseWeight(Long baseWeight) {
		this.baseWeight = baseWeight;
	}

	
	public boolean isClusterIdEnabled() {
		return clusterIdEnabled;
	}

	
	public void setClusterIdEnabled(boolean clusterIdEnabled) {
		this.clusterIdEnabled = clusterIdEnabled;
	}

	
	public boolean isFilteringEnabled() {
		return filteringEnabled;
	}

	
	public void setFilteringEnabled(boolean filteringEnabled) {
		this.filteringEnabled = filteringEnabled;
	}

	
	public boolean isInterceptionEnabled() {
		return interceptionEnabled;
	}

	
	public void setInterceptionEnabled(boolean interceptionEnabled) {
		this.interceptionEnabled = interceptionEnabled;
	}

	
	public boolean isLibrary() {
		return isLibrary;
	}

	
	public void setLibrary(boolean library) {
		isLibrary = library;
	}
}
