package cz.mzk.recordmanager.api.model.statistics;

import java.util.Date;
import java.util.List;

public class StatisticDetailsDto extends GeneralStatisticsDto {

	private Long version;

	private Long jobInstanceId;

	private Date createTime;

	private String exitCode;

	private String exitMessage;

	private Date lastUpdated;

	private String jobConfigurationLocation;

	private String jobName;

	private String jobKey;

	private List<JobParameterDto> jobParameter;

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getJobInstanceId() {
		return jobInstanceId;
	}

	public void setJobInstanceId(Long jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
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


	public String getJobConfigurationLocation() {
		return jobConfigurationLocation;
	}

	public void setJobConfigurationLocation(String jobConfigurationLocation) {
		this.jobConfigurationLocation = jobConfigurationLocation;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public List<JobParameterDto> getJobParameter() {
		return jobParameter;
	}

	public void setJobParameter(List<JobParameterDto> jobParameter) {
		this.jobParameter = jobParameter;
	}

	public String getJobKey() {
		return jobKey;
	}

	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
