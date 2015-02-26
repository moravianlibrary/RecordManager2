package cz.mzk.recordmanager.server.model.batch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=BatchJobInstance.TABLE)
public class BatchJobInstance {
	
	public static final String TABLE = "batch_job_instance";

	@Id
	@Column(name="job_instance_id")
	private Long id;
	
	@Column(name="job_name")
	private String name;

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
	
}
