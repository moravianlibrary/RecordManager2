package cz.mzk.recordmanager.api.model.configurations;

import cz.mzk.recordmanager.api.model.ContactPersonDto;
import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.LibraryDto;

public class ImportConfigurationDto extends IdDto {

	private LibraryDto library;

	private String idPrefix;

	private ContactPersonDto contact;

	private boolean isThisLibrary = false;

	private Long baseWeight;

	private boolean clusterIdEnabled;

	private boolean filteringEnabled = false;

	private boolean interceptionEnabled = false;

	private String configurationType;


	public LibraryDto getLibrary() {
		return library;
	}

	public void setLibrary(LibraryDto library) {
		this.library = library;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	public ContactPersonDto getContact() {
		return contact;
	}

	public void setContact(ContactPersonDto contact) {
		this.contact = contact;
	}

	public boolean isThisLibrary() {
		return isThisLibrary;
	}

	public void setThisLibrary(boolean thisLibrary) {
		isThisLibrary = thisLibrary;
	}


	public String getConfigurationType() {
		return configurationType;
	}

	public void setConfigurationType(String configurationType) {
		this.configurationType = configurationType;
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
}
