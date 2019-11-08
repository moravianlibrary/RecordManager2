package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter.ParameterType;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

import java.util.Collection;
import java.util.Collections;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class RegenerateJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public void validate(JobParameters parameters)
			throws JobParametersInvalidException {
		super.validate(parameters);
	}

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Collections.singletonList(
				param(Constants.JOB_PARAM_RECORD_ID, ParameterType.LONG, false)
		);
	}
}
