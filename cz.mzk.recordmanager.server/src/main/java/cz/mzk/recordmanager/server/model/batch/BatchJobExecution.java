package cz.mzk.recordmanager.server.model.batch;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name=BatchJobExecution.TABLE)
public class BatchJobExecution {
	
	public static final String TABLE = "batch_job_execution";
	
	@Id
	@Column(name="job_execution_id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "job_instance_id")
	private BatchJobInstance jobInstance;
	
	@Column(name="create_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date create;
	
	@Column(name="start_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date start;
	
	@Column(name="end_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date end;
	
	@Column(name="status")
	private String status;
	
	@Column(name="exit_code")
	private String exitCode;
	
	@Column(name="exit_message")
	private String exitMessage;
	
	@OneToMany(mappedBy="execution")
	private List<BatchJobExecutionStep> steps;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BatchJobInstance getJobInstance() {
		return jobInstance;
	}

	public void setJobInstance(BatchJobInstance jobInstance) {
		this.jobInstance = jobInstance;
	}

	public Date getCreate() {
		return create;
	}

	public void setCreate(Date create) {
		this.create = create;
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

	public List<BatchJobExecutionStep> getSteps() {
		return steps;
	}

}
