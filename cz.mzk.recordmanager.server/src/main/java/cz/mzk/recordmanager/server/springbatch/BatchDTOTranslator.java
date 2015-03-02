package cz.mzk.recordmanager.server.springbatch;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;

@Component
public class BatchDTOTranslator {

	public BatchJobExecutionDTO translate(BatchJobExecution jobExec) {
		BatchJobExecutionDTO dto = new BatchJobExecutionDTO();
		dto.setId(jobExec.getId());
		return dto;
	}

}
