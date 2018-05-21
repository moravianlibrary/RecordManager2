package cz.mzk.recordmanager.server.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

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
	private boolean filteringEnabled = false;

	@Column(name="interception_enabled")
	private boolean interceptionEnabled = false;

	@Column(name="is_library")
	private boolean isLibrary = false;

	@Type(
		type="cz.mzk.recordmanager.server.hibernate.CharEnumUserType",
		parameters= {
			@Parameter(name="enumClassName", value="cz.mzk.recordmanager.server.model.HarvestFrequency"),
		}
	)
	@Column(name="harvest_frequency")
	private HarvestFrequency harvestFrequency = HarvestFrequency.UNSPECIFIED;

	@Column(name="mapping_script")
	private String mappingScript;

	@Column(name = "mapping_dedup_script")
	private String mappingDedupScript;

	@Column(name="generate_dedup_keys")
	private boolean generateDedupKeys = true;

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="import_conf_id", referencedColumnName="id")
	private List<Sigla> siglas = new ArrayList<>();

	@Type(
			type = "cz.mzk.recordmanager.server.hibernate.StringEnumUserType",
			parameters = {
					@Parameter(name = "enumClassName", value = "cz.mzk.recordmanager.server.model.ItemId"),
			}
	)
	@Column(name = "item_id")
	private ItemId itemId;

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

	public boolean isInterceptionEnabled() {
		return interceptionEnabled;
	}

	public void setInterceptionEnabled(boolean interceptionEnabled) {
		this.interceptionEnabled = interceptionEnabled;
	}

	public boolean isLibrary() {
		return isLibrary;
	}

	public void setLibrary(boolean isLibrary) {
		this.isLibrary = isLibrary;
	}

	public HarvestFrequency getHarvestFrequency() {
		return harvestFrequency;
	}

	public void setHarvestFrequency(HarvestFrequency harvestFrequency) {
		this.harvestFrequency = harvestFrequency;
	}

	public String getMappingScript() {
		return mappingScript;
	}

	public void setMappingScript(String mappingScript) {
		this.mappingScript = mappingScript;
	}

	public boolean isGenerateDedupKeys() {
		return generateDedupKeys;
	}

	public void setGenerateDedupKeys(boolean generateDedupKeys) {
		this.generateDedupKeys = generateDedupKeys;
	}

	public String getMappingDedupScript() {
		return mappingDedupScript;
	}

	public void setMappingDedupScript(String mappingDedupScript) {
		this.mappingDedupScript = mappingDedupScript;
	}

	public List<Sigla> getSiglas() {
		return siglas;
	}

	public void setSiglas(List<Sigla> siglas) {
		this.siglas = siglas;
	}

	public ItemId getItemId() {
		return itemId;
	}

	public void setItemId(ItemId itemId) {
		this.itemId = itemId;
	}

}
