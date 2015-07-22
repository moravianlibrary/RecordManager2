package cz.mzk.recordmanager.server.springbatch;

import java.util.UUID;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

public enum UUIDIncrementer implements JobParametersIncrementer {

	INSTANCE;

	@Override
	public JobParameters getNext(JobParameters parameters) {
		JobParametersBuilder builder = new JobParametersBuilder(parameters);
		if (parameters.getString("uuid", null) == null) {
			UUID uuid = UUID.randomUUID();
			builder.addString("uuid", uuid.toString());
		}
		return builder.toJobParameters();
	}

}
