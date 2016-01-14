package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Arrays;
import java.util.Collection;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParameter.ParameterType;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class OAIHarvestJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public void validate(JobParameters parameters)
			throws JobParametersInvalidException {
		super.validate(parameters);
	}

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_CONF_ID, ParameterType.LONG, true), //
				param(Constants.JOB_PARAM_FROM_DATE, ParameterType.DATE, false), //
				param(Constants.JOB_PARAM_UNTIL_DATE, ParameterType.DATE, false), //
				param(Constants.JOB_PARAM_RESUMPTION_TOKEN, ParameterType.STRING, false), //
				param(Constants.JOB_PARAM_INCREMENTAL, ParameterType.LONG, false), //
				param(Constants.JOB_PARAM_START_TIME, ParameterType.DATE, false), //
				param(Constants.JOB_PARAM_REHARVEST, ParameterType.LONG, false) //
				);
	}

}
