package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;

public interface BatchService {

	public List<BatchJobExecutionDTO> getRunningJobExecutions();

	public void restart(BatchJobExecutionDTO jobExecution);

}
