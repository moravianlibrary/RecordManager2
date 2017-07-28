package cz.mzk.recordmanager.server.facade;

import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RegenerateMissingDedupKeysJobImpl implements RegenerateMissingDedupKeysJob {

	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void runRegenerateMissingDedupKeysJob() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(Constants.JOB_ID_REGEN_MISSING_DEDUP_KEYS, params);
	}

}
