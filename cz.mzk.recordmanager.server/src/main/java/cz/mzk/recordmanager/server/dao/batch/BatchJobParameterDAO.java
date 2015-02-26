package cz.mzk.recordmanager.server.dao.batch;

import java.util.List;

import cz.mzk.recordmanager.server.model.batch.BatchJobParam;
import cz.mzk.recordmanager.server.model.batch.BatchJobParam.JobParamId;
import cz.mzk.recordmanager.server.oai.dao.DomainDAO;

public interface BatchJobParameterDAO extends DomainDAO<JobParamId, BatchJobParam> {

	public List<BatchJobParam> findByJobExecutionId(Long jobInstanceId);

}
