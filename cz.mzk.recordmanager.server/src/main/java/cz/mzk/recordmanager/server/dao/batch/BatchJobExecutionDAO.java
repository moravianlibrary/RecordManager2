package cz.mzk.recordmanager.server.dao.batch;

import java.util.List;

import cz.mzk.recordmanager.api.model.batch.JobExecutionQueryDTO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;
import cz.mzk.recordmanager.server.oai.dao.DomainDAO;

public interface BatchJobExecutionDAO extends DomainDAO<Long, BatchJobExecution> {

	public List<BatchJobExecution> getExecutions(JobExecutionQueryDTO query); 
	
	public List<BatchJobExecution> getRunningExecutions();
	
}
