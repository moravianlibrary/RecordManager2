package cz.mzk.recordmanager.server.adresar;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameter.ParameterType;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;

public class AdresarHarvestJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_CONF_ID, ParameterType.LONG, true),
				param(Constants.JOB_PARAM_FIRST_ID, ParameterType.STRING, false), //
				param(Constants.JOB_PARAM_LAST_ID, ParameterType.STRING, false), //
				param(Constants.JOB_PARAM_SINGLE_ID, ParameterType.STRING, false) //
			);
	}

}
