package cz.mzk.recordmanager.api.model.batch;

import java.io.Serializable;
import java.util.Date;

public class OaiHarvestJobStatisticsDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long jobExecutionId;

	private Long importConfId;

	private String libraryName;

	private String url;

	private String setSpec;

	private Date startTime;

	private Date endTime;

	private String status;

	private Date fromParam;

	private Date toParam;

	private Long noOfRecords;

	public Long getImportConfId() {
		return importConfId;
	}

	public void setImportConfId(Long importConfId) {
		this.importConfId = importConfId;
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

	public String getSetSpec() {
		return setSpec;
	}

	public void setSetSpec(String setSpec) {
		this.setSpec = setSpec;
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

	public Date getFromParam() {
		return fromParam;
	}

	public void setFromParam(Date fromParam) {
		this.fromParam = fromParam;
	}

	public Date getToParam() {
		return toParam;
	}

	public void setToParam(Date toParam) {
		this.toParam = toParam;
	}

	public Long getNoOfRecords() {
		return noOfRecords;
	}

	public void setNoOfRecords(Long noOfRecords) {
		this.noOfRecords = noOfRecords;
	}

	public Long getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}

}
