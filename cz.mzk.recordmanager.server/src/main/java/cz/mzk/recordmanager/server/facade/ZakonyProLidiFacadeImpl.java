package cz.mzk.recordmanager.server.facade;

import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ZakonyProLidiFacadeImpl implements ZakonyProLidiFacade {

	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void runZakonyProLidiHarvestJob() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(Constants.IMPORT_CONF_ID_ZAKONY));
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("ZakonyProLidi harvest failed", exec);
		}
	}

	@Override
	public void runZakonyProLidiFulltextJob() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(Constants.IMPORT_CONF_ID_ZAKONY));
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_FULLTEXT_ZAKONYPROLIDI, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("ZakonyProLidi fulltext failed", exec);
		}
	}

}
