package cz.mzk.recordmanager.api.model.batch;

import java.io.Serializable;

public class BatchJobExecutionDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
