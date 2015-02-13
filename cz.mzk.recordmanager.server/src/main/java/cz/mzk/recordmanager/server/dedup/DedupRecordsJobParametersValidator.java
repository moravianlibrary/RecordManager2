package cz.mzk.recordmanager.server.dedup;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameter.ParameterType;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;

public class DedupRecordsJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public void validate(JobParameters parameters)
			throws JobParametersInvalidException {
		super.validate(parameters);
	}

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_CONF_ID, ParameterType.LONG, false), //
				param(Constants.JOB_PARAM_FROM_DATE, ParameterType.DATE, false) //
				);
	}
}
