package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;

public interface BatchService {

	public List<BatchJobExecutionDTO> getRunningJobExecutions();
	
	public BatchJobExecutionDTO getJobExecution(long id);

	public void restart(BatchJobExecutionDTO jobExecution);

	public BatchJobExecutionDTO getJobExecution(Long id);

	public void runFullHarvest(Long id);

	public void runIncrementalHarvest(Long id);

	public void runDeduplicate();

	public void runDownloadAndImport(Long id);

	public void runIndex();

}
