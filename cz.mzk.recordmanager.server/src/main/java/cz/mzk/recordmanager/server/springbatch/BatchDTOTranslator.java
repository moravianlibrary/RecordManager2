package cz.mzk.recordmanager.server.springbatch;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;

@Component
public class BatchDTOTranslator {

	public BatchJobExecutionDTO translate(BatchJobExecution jobExec) {
		BatchJobExecutionDTO dto = new BatchJobExecutionDTO();
		dto.setId(jobExec.getId());
		dto.setJobInstanceID(jobExec.getJobInstance().getId());
		dto.setCreateTime(jobExec.getCreate());
		dto.setStartTime(jobExec.getStart());
		dto.setEndTime(jobExec.getEnd());
		dto.setStatus(jobExec.getStatus());
		dto.setExitMessage(jobExec.getExitMessage());
		return dto;
	}

}
