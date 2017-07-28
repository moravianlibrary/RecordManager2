package cz.mzk.recordmanager.api.model;

import java.io.Serializable;

public class LibraryDto extends IdDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String url;

	private String catalogUrl;

	private String city;

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

}
