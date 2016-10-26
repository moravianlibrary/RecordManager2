package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;

public interface BatchService {

	public List<BatchJobExecutionDTO> getRunningJobExecutions();
	
	public BatchJobExecutionDTO getJobExecution(long id);

	public void restart(BatchJobExecutionDTO jobExecution);

	public BatchJobExecutionDTO getJobExecution(Long id);

	public void runFullHarvest(IdDto id);

	public void runIncrementalHarvest(IdDto id);

	public void runDeduplicate();

	public void runDownloadAndImport(IdDto id);

	public void runIndex();

}
