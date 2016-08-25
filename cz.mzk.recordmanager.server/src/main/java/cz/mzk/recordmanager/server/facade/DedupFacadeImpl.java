package cz.mzk.recordmanager.server.facade;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class DedupFacadeImpl implements DedupFacade {

	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void deduplicate() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(Constants.JOB_ID_DEDUP, params);
	}

}
