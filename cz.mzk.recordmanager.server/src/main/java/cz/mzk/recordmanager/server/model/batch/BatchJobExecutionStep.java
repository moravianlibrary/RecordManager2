package cz.mzk.recordmanager.server.model.batch;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name=BatchJobExecutionStep.TABLE)
public class BatchJobExecutionStep {
	
	public static final String TABLE = "batch_step_execution";
	
	@Id
	@Column(name="step_execution_id")
	private Long id;
	
	@Column(name="step_name")
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "job_execution_id")
	private BatchJobExecution execution;
	
	@Column(name="start_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date start;
	
	@Column(name="end_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date end;

	@Column(name="status")
	private String status;
	
	@Column(name="commit_count")
	private int commitCount;
	
	@Column(name="read_count")
	private int readCount;
	
	@Column(name="filter_count")
	private int filterCount;
	
	@Column(name="write_count")
	private int writeCount;
	
	@Column(name="exit_code")
	private String exitCode;
	
	@Column(name="exit_message")
	private String exitMessage;
	
	@Column(name="last_updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BatchJobExecution getExecution() {
		return execution;
	}

	public void setExecution(BatchJobExecution execution) {
		this.execution = execution;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getCommitCount() {
		return commitCount;
	}

	public void setCommitCount(int commitCount) {
		this.commitCount = commitCount;
	}

	public int getReadCount() {
		return readCount;
	}

	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}

	public int getFilterCount() {
		return filterCount;
	}

	public void setFilterCount(int filterCount) {
		this.filterCount = filterCount;
	}

	public int getWriteCount() {
		return writeCount;
	}

	public void setWriteCount(int writeCount) {
		this.writeCount = writeCount;
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

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
}
