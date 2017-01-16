package cz.mzk.recordmanager.api.model.configurations;

import java.io.Serializable;

public class DownloadImportConfigurationDto extends ImportConfigurationDto implements Serializable{

	private static final long serialVersionUID = 1L;

	private String url;

	private String format;

	private String jobName;

	private String regex;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
}
