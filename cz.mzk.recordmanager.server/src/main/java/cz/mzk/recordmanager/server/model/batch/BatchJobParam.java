package cz.mzk.recordmanager.server.model.batch;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=BatchJobParam.TABLE)
public class BatchJobParam {
	
	public static final String TABLE = "batch_job_execution_params";
	
	@Embeddable
	public static class JobParamId implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@Column(name="job_execution_id")
		private Long jobExecutionId;
		
		@Column(name="key_name")
		private String keyName;

		public Long getJobExecutionId() {
			return jobExecutionId;
		}

		public void setJobExecutionId(Long jobExecutionId) {
			this.jobExecutionId = jobExecutionId;
		}

		public String getKeyName() {
			return keyName;
		}

		public void setKeyName(String keyName) {
			this.keyName = keyName;
		}
		
	}
	
	@Id
	private JobParamId id;
	
	@Column(name="type_cd")
	private String valueType;
	
	@Column(name="string_val")
	private String stringValue;
	
	@Column(name="date_val")
	private Date dateValue;
	
	@Column(name="long_val")
	private Long longValue;
	
	@Column(name="double_val")
	private Double doubleValue;

	public JobParamId getId() {
		return id;
	}

	public void setId(JobParamId id) {
		this.id = id;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}
	
}
