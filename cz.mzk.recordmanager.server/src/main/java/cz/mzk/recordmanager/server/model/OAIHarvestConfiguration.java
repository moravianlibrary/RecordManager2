package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Preconditions;

@Entity
@Table(name=OAIHarvestConfiguration.TABLE_NAME)
public class OAIHarvestConfiguration extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "oai_harvest_conf";
	
	@ManyToOne(optional=false)
	@JoinColumn(name="library_id", nullable=false)
	private Library library;
	
	@Column(name="url")
	private String url;
	
	@Column(name="set_spec")
	private String set;
	
	@Column(name="granularity")
	@Enumerated(EnumType.STRING)
	private OAIGranularity granularity;

	@Column(name="metadata_prefix")
	private String metadataPrefix;

	@ManyToOne(optional=false)
	@JoinColumn(name="contact_person_id")
	private ContactPerson contact;

	@Column(name="id_prefix")
	private String idPrefix;

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		Preconditions.checkNotNull(library, "library");
		this.library = library;
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
	
	public OAIGranularity getGranularity() {
		return granularity;
	}

	public void setGranularity(OAIGranularity granularity) {
		this.granularity = granularity;
	}

	public String getMetadataPrefix() {
		return metadataPrefix;
	}

	public void setMetadataPrefix(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

}
