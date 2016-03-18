package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name=OAIHarvestConfiguration.TABLE_NAME)
@PrimaryKeyJoinColumn(name="import_conf_id")
public class OAIHarvestConfiguration extends ImportConfiguration {
	
	public static final String TABLE_NAME = "oai_harvest_conf";
	
	@Column(name="url")
	private String url;
	
	@Column(name="set_spec")
	private String set;
	
	@Column(name="granularity")
	@Enumerated(EnumType.STRING)
	private OAIGranularity granularity;

	@Column(name="metadata_prefix")
	private String metadataPrefix;

	@Column(name="extract_id_regex")
	private String regex;

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

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	@Override
	public String toString() {
		return String.format("%s[id=%s, url='%s', set='%s']", this.getClass().getSimpleName(),
				this.getId(), this.getUrl(), this.getSet());
	}

}
