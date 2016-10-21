package cz.mzk.recordmanager.server.service;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.model.batch.JobExecutionQueryDTO;
import cz.mzk.recordmanager.api.service.BatchService;
import cz.mzk.recordmanager.server.dao.batch.BatchJobExecutionDAO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class BatchServiceImpl implements BatchService {

	@Autowired
	private BatchJobExecutionDAO batchJobExecutionDAO;

	@Override
	public List<BatchJobExecutionDTO> getRunningJobExecutions() {
		JobExecutionQueryDTO query = new JobExecutionQueryDTO();
		query.setStatus("STARTED");
		query.setExitCode("UNKNOWN");
		List<BatchJobExecution> jobs = batchJobExecutionDAO.getExecutions(query);
		return jobs.stream().map(it -> translate(it)).collect(Collectors.toList());
	}

	@Override
	public BatchJobExecutionDTO getJobExecution(long id) {
		return null;
	}

	@Override
	public void restart(BatchJobExecutionDTO jobExecution) {
	}

	private BatchJobExecutionDTO translate(BatchJobExecution execution) {
		BatchJobExecutionDTO dto = new BatchJobExecutionDTO();
		dto.setId(execution.getId());
		return dto;
	}

}
