package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = KrameriusConfiguration.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "import_conf_id", referencedColumnName = "id")
public class KrameriusConfiguration extends ImportConfiguration {
	
	public static final String TABLE_NAME = "kramerius_conf";
	
	@Column(name="url")
	private String url;
	
	@Column(name="model")
	private String model;
	
	@Column(name="query_rows")
	private Long queryRows;
	
	@Column(name="metadata_stream")
	private String metadataStream;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Long getQueryRows() {
		return queryRows;
	}

	public void setQueryRows(Long queryRows) {
		this.queryRows = queryRows;
	}

	public String getMetadataStream() {
		return metadataStream;
	}

	public void setMetadataStream(String metadataStream) {
		this.metadataStream = metadataStream;
	}
	
	
}
