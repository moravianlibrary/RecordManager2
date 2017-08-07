package cz.mzk.recordmanager.api.model.batch;

import java.io.Serializable;
import java.util.Date;

public class BatchJobExecutionDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Long jobInstanceID;

	private Date createTime;

	private Date startTime;

	private Date endTime;

	private String status;

	private String exitCode;

	private String exitMessage;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getJobInstanceID() {
		return jobInstanceID;
	}

	public void setJobInstanceID(Long jobInstanceID) {
		this.jobInstanceID = jobInstanceID;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public String getExitCode() {
		return exitCode;
	}

	public void setExitCode(String exitCode) {
		this.exitCode = exitCode;
	}

	public String getExitMessage() {
		return exitMessage;
	}

	public void setExitMessage(String exitMessage) {
		this.exitMessage = exitMessage;
	}

}
