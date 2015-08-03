package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = ImportConfiguration.TABLE_NAME)
public abstract class ImportConfiguration extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "import_conf";
	
	@ManyToOne(optional=false)
	@JoinColumn(name="library_id", nullable=false)
	private Library library;

	@ManyToOne(optional=false)
	@JoinColumn(name="contact_person_id")
	private ContactPerson contact;
	
	@Column(name="id_prefix")
	private String idPrefix;
	
	@Column(name="base_weight")
	private Long baseWeight;
	
	@Column(name="cluster_id_enabled")
	private boolean clusterIdEnabled;
	
	@Column(name="filtering_enabled")
	private boolean filteringEnabled;

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	public ContactPerson getContact() {
		return contact;
	}

	public void setContact(ContactPerson contact) {
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

}
