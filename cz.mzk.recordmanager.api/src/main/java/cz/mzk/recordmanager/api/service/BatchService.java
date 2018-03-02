package cz.mzk.recordmanager.api.service;

import java.io.File;
import java.util.List;

import cz.mzk.recordmanager.api.model.IdDto;

import cz.mzk.recordmanager.api.model.RecordIdDto;
import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;

public interface BatchService {

	List<BatchJobExecutionDTO> getRunningJobExecutions();

	BatchJobExecutionDTO getJobExecution(long id);

	void restart(BatchJobExecutionDTO jobExecutionId);

	BatchJobExecutionDTO getJobExecution(Long id);

	void runFullHarvest(List<IdDto> id);

	void runIncrementalHarvest(List<IdDto> id);

	void runDeduplicate();

	void runIndex();

	void runIndividualIndex(List<RecordIdDto> ids);

	void runImportRecordsJob(Long id, File file, String format);

	void runFilterCaslinRecordsJob();

	void runRegenerateDedupKeysJob();

	void runRegenerateMissingDedupKeysJob();

}
