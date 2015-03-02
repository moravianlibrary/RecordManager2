package cz.mzk.recordmanager.server.springbatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.api.service.BatchService;
import cz.mzk.recordmanager.server.dao.batch.BatchJobExecutionDAO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;

@Component
public class BatchServiceImpl implements BatchService {

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private BatchJobExecutionDAO batchJobExecutionDao;

	public void restartRunningJobs() {
		for (BatchJobExecution job : batchJobExecutionDao.getRunningExecutions()) {
			jobExecutor.restart(job.getId());
		}
	}

}
