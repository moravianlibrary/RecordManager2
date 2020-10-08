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

	@Column(name="format")
	private String format;

	@Column(name="import_job_name")
	private String jobName;

	@Column(name="extract_id_regex")
	private String regex;

	@Column(name = "reharvest")
	private boolean reharvest;

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

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

	public boolean isReharvest() {
		return reharvest;
	}

	public void setReharvest(boolean reharvest) {
		this.reharvest = reharvest;
	}

}
