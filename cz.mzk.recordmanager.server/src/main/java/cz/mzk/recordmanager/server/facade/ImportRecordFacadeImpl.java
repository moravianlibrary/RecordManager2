package cz.mzk.recordmanager.server.facade;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class ImportRecordFacadeImpl implements ImportRecordFacade {

	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void importFile(long importConfId, File file, String format) {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(file.getAbsolutePath()));
		parameters.put(Constants.JOB_PARAM_FORMAT, new JobParameter(format));
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(importConfId));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_IMPORT, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("Incremental harvest failed", exec);
		}
	}

}
