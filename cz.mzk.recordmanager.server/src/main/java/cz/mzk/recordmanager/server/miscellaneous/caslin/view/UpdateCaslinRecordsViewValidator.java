package cz.mzk.recordmanager.server.miscellaneous.caslin.view;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

import java.util.Arrays;
import java.util.Collection;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class UpdateCaslinRecordsViewValidator extends DefaultJobParametersValidator {

	@Override
	public void validate(JobParameters parameters) throws JobParametersInvalidException {
		super.validate(parameters);
	}

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_VIEW, JobParameter.ParameterType.STRING, true),
				param(Constants.JOB_PARAM_UNTIL_DATE, JobParameter.ParameterType.DATE, false)
		);
	}

}
