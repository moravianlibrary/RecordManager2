package cz.mzk.recordmanager.server.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name=Library.TABLE_NAME)
public class Library extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "library";
	
	@Column(name="name")
	private String name;
	
	@Column(name="url")
	private String url;
	
	@Column(name="catalog_url")
	private String catalogUrl;
	
	@Column(name="city")
	private String city;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="library", orphanRemoval=true)
	private List<ImportConfiguration> inputConfigurations = new ArrayList<ImportConfiguration>();
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="library", orphanRemoval=true)
	private List<ContactPerson> contacts = new ArrayList<ContactPerson>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCatalogUrl() {
		return catalogUrl;
	}

	public void setCatalogUrl(String catalogUrl) {
		this.catalogUrl = catalogUrl;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public List<ImportConfiguration> getOaiHarvestConfigurations() {
		return inputConfigurations;
	}
	
	public List<ContactPerson> getContacts() {
		return contacts;
	}
	
}
