package cz.mzk.recordmanager.server.springbatch;

import java.util.UUID;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

import cz.mzk.recordmanager.server.util.Constants;

public enum UUIDIncrementer implements JobParametersIncrementer {

	INSTANCE;

	@Override
	public JobParameters getNext(JobParameters parameters) {
		JobParametersBuilder builder = new JobParametersBuilder(parameters);
		if (parameters.getString(Constants.JOB_PARAM_UUID, null) == null) {
			UUID uuid = UUID.randomUUID();
			builder.addString(Constants.JOB_PARAM_UUID, uuid.toString());
		}
		return builder.toJobParameters();
	}

}
