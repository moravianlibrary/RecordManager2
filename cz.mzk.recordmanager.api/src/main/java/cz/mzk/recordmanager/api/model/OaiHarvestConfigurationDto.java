package cz.mzk.recordmanager.api.model;

import java.io.Serializable;

public class OaiHarvestConfigurationDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String url;

	private String set;

	private String metadataPrefix;

	private ContactPersonDto contactPerson;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}

	public String getMetadataPrefix() {
		return metadataPrefix;
	}

	public void setMetadataPrefix(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}

	public ContactPersonDto getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(ContactPersonDto contactPerson) {
		this.contactPerson = contactPerson;
	}

}
