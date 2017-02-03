package cz.mzk.recordmanager.api.model.statistics;


import java.util.Date;

public class DownloadImportConfJobStatisticsDto {
	private Long jobExecutionId;
	private Long importConfId;
	private String libraryName;
	private String url;
	private String importJobName;
	private String format;
	private Date startTime;
	private Date endTime;
	private String status;
	private Long noOfRecords;

	public Long getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}


	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImportJobName() {
		return importJobName;
	}

	public void setImportJobName(String importJobName) {
		this.importJobName = importJobName;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getNoOfRecords() {
		return noOfRecords;
	}

	public void setNoOfRecords(Long noOfRecords) {
		this.noOfRecords = noOfRecords;
	}

	public Long getImportConfId() {
		return importConfId;
	}

	public void setImportConfId(Long importConfId) {
		this.importConfId = importConfId;
	}
}
