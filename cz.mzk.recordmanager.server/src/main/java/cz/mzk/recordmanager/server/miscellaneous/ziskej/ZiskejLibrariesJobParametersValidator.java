package cz.mzk.recordmanager.server.miscellaneous.ziskej;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

import java.util.Arrays;
import java.util.Collection;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class ZiskejLibrariesJobParametersValidator extends DefaultJobParametersValidator {

	@Override
	public void validate(JobParameters parameters) throws JobParametersInvalidException {
		super.validate(parameters);
	}

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_FORMAT, JobParameter.ParameterType.STRING, true),
				param(Constants.JOB_PARAM_REHARVEST, JobParameter.ParameterType.STRING, false)
		);
	}

}
