package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.oai.harvest.SourceMapping;

import javax.persistence.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = ImportConfigurationMappingField.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "import_conf_id")
public class ImportConfigurationMappingField extends ImportConfiguration {

	public static final String TABLE_NAME = "import_conf_mapping_field";

	private static final Pattern FIELD_VALUE = Pattern.compile("([0-9]{3})\\$(.)(.*)", Pattern.CASE_INSENSITIVE);

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_import_conf_id", nullable = false, updatable = false, insertable = false)
	private ImportConfiguration parentImportConfiguration;

	@Column(name = "parent_import_conf_id")
	private Long parentImportConfId;

	@Column(name = "mapping")
	private String mapping;

	public SourceMapping getSourceMapping() {
		Matcher matcher = FIELD_VALUE.matcher(this.mapping);
		if (matcher.matches()) {
			return new SourceMapping(this, matcher.group(1), matcher.group(2).charAt(0), matcher.group(3));
		}
		return null;
	}

	public Long getParentImportConfId() {
		return parentImportConfId;
	}

	public void setParentImportConfId(Long parentImportConfId) {
		this.parentImportConfId = parentImportConfId;
	}

	public ImportConfiguration getParentImportConfiguration() {
		return parentImportConfiguration;
	}

	public void setParentImportConfiguration(ImportConfiguration parentImportConf) {
		this.parentImportConfiguration = parentImportConf;
	}

	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	@Override
	public String toString() {
		return "ImportConfigurationMappingField{" +
				"importConfId=" + super.getId() +
				", parentImportConfId=" + parentImportConfId +
				", mapping='" + mapping + '\'' +
				'}';
	}

}
