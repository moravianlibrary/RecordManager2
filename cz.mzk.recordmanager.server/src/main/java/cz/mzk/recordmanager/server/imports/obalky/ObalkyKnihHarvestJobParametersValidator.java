package cz.mzk.recordmanager.server.imports.obalky;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter;

import java.util.Arrays;
import java.util.Collection;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class ObalkyKnihHarvestJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_IN_FILE, JobParameter.ParameterType.STRING, false),
				param(Constants.JOB_PARAM_FROM_DATE, JobParameter.ParameterType.DATE, false)
		);
	}
}
