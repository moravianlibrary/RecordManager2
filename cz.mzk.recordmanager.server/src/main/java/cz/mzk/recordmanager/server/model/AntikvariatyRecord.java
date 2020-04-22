package cz.mzk.recordmanager.server.model;

import org.apache.commons.collections.CollectionUtils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = AntikvariatyRecord.TABLE_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "record")
public class AntikvariatyRecord {

	public static final String TABLE_NAME = "antikvariaty";

	@Id
	@Column(name = "id")
	@XmlElement(name = "identifier")
	private Long id;

	@Column(name = "url")
	@XmlElement(name = "url")
	private String url;

	@Column(name = "title")
	@XmlElement(name = "title")
	private String title;

	@Column(name = "updated_original")
	@Temporal(TemporalType.TIMESTAMP)
	@XmlElement(name = "datestamp")
	private Date updatedOriginal;

	@Column(name = "pub_year")
	@XmlElement(name = "date")
	private Long publicationYear;

	@ElementCollection
	@CollectionTable(name = "antikvariaty_catids", joinColumns = @JoinColumn(name = "antikvariaty_id"))
	@Column(name = "id_from_catalogue")
	@XmlElement(name = "ctlno")
	private List<String> catalogueIds = new ArrayList<>();

	@Column(name = "last_harvest")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastHarvest;

	@Column(name = "updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

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

	public Date getLastHarvest() {
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}

	public Date getUpdatedOriginal() {
		return updatedOriginal;
	}

	public void setUpdatedOriginal(Date updatedOriginal) {
		this.updatedOriginal = updatedOriginal;
	}

	@Override
	public String toString() {
		return "AntikvariatyRecord{" +
				"id=" + id +
				", url='" + url + '\'' +
				", title='" + title + '\'' +
				", publicationYear=" + publicationYear +
				", catalogueIds=" + catalogueIds +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AntikvariatyRecord that = (AntikvariatyRecord) o;
		return id.equals(that.id) &&
				Objects.equals(url, that.url) &&
				Objects.equals(title, that.title) &&
				Objects.equals(publicationYear, that.publicationYear) &&
				CollectionUtils.isEqualCollection(catalogueIds, that.catalogueIds);
	}

	@Override
	public int hashCode() {
		return id.intValue();
	}
}
