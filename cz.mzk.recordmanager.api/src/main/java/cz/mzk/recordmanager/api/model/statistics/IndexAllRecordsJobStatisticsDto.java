package cz.mzk.recordmanager.api.model.statistics;

import java.util.Date;

public class IndexAllRecordsJobStatisticsDto {

	private Long jobExecutionId;

	private Date startTime;

	private Date endTime;

	private String status;

	private Date fromParam;

	private Date toParam;

	private String stringVal;

	public Long getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
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

	public String getStringVal() {
		return stringVal;
	}

	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
	}

}
