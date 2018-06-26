package cz.mzk.recordmanager.server.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name=AntikvariatyRecord.TABLE_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "record")
public class AntikvariatyRecord {

	public static final String TABLE_NAME = "antikvariaty";
	
	@Id
	@Column(name="id")
	@XmlElement(name="identifier")
	private Long id;
	
	@Column(name="url")
	@XmlElement(name="url")
    private String url;
	
	@Column(name="title")
	@XmlElement(name="title")
    private String title;
	
	@Column(name="updated")
    @Temporal(TemporalType.TIMESTAMP)
	@XmlElement(name="datestamp")
	private Date updated;
    
	@Column(name="pub_year")
	@XmlElement(name="date")
    private Long publicationYear;
    
	@ElementCollection
	@CollectionTable(name="antikvariaty_catids",joinColumns=@JoinColumn(name="antikvariaty_id"))
	@Column(name="id_from_catalogue")
	@XmlElement(name="ctlno")
	private List<String> catalogueIds = new ArrayList<>();

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Long getPublicationYear() {
		return publicationYear;
	}
	
	public void setPublicationYear(Long publicationYear) {
		this.publicationYear = publicationYear;
	}

	public List<String> getCatalogueIds() {
		return catalogueIds;
	}

	public void setCatalogueIds(List<String> catalogueIds) {
		this.catalogueIds = catalogueIds;
	}

	@Override
	public String toString() {
		return "AntikvariatyRecord [url=" + url + ", updated=" + updated + ']';
	}	
	
}
