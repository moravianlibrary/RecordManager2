package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = DownloadImportConfiguration.TABLE_NAME)
@PrimaryKeyJoinColumn(name="import_conf_id")
public class DownloadImportConfiguration extends ImportConfiguration {
	
	public static final String TABLE_NAME = "download_import_conf";
	
	@Column(name="url")
	private String url;

	@Column(name="import_job_name")
	private String jobName;
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
}
