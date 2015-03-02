package cz.mzk.recordmanager.server.springbatch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.service.BatchService;
import cz.mzk.recordmanager.server.dao.batch.BatchJobExecutionDAO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;

@Component
public class BatchServiceImpl implements BatchService {

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private BatchJobExecutionDAO batchJobExecutionDao;

	@Autowired
	private BatchDTOTranslator dtoTranslator;
	
	@Transactional(readOnly=true)
	@Override
	public List<BatchJobExecutionDTO> getRunningJobExecutions() {
		List<BatchJobExecution> jobs = batchJobExecutionDao.getRunningExecutions();
		List<BatchJobExecutionDTO> dtos = new ArrayList<BatchJobExecutionDTO>();
		for (BatchJobExecution job : jobs) {
			dtos.add(dtoTranslator.translate(job));
		}
		return dtos;
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void restart(BatchJobExecutionDTO jobExecution) {
		jobExecutor.restart(jobExecution.getId());
	}

}
