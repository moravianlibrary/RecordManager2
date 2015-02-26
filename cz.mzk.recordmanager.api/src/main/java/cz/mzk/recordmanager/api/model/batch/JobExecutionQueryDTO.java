package cz.mzk.recordmanager.api.model.batch;

import java.io.Serializable;
import java.util.Date;

public class JobExecutionQueryDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	private Date startedFrom;
	
	private Date startedTo;
	
	private int limit = 10;
	
	private int offset = 0;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Date getStartedFrom() {
		return startedFrom;
	}

	public void setStartedFrom(Date startedFrom) {
		this.startedFrom = startedFrom;
	}

	public Date getStartedTo() {
		return startedTo;
	}

	public void setStartedTo(Date startedTo) {
		this.startedTo = startedTo;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

}
