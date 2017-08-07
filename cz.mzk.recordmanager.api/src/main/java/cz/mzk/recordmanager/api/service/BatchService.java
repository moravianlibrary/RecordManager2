package cz.mzk.recordmanager.api.service;

import java.io.File;
import java.util.List;

import cz.mzk.recordmanager.api.model.IdDto;

import cz.mzk.recordmanager.api.model.RecordIdDto;
import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;

public interface BatchService {

	public List<BatchJobExecutionDTO> getRunningJobExecutions();

	public BatchJobExecutionDTO getJobExecution(long id);

	public void restart(BatchJobExecutionDTO jobExecutionId);

	public BatchJobExecutionDTO getJobExecution(Long id);

	public void runFullHarvest(List<IdDto> id);

	public void runIncrementalHarvest(List<IdDto> id);

	public void runDeduplicate();

	public void runIndex();

	public void runIndividualIndex(List<RecordIdDto> ids);

	public void runImportRecordsJob(Long id, File file, String format);

	public void runFilterCaslinRecordsJob();

	public void runRegenerateDedupKeysJob();

	public void runRegenerateMissingDedupKeysJob();

}
