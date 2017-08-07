package cz.mzk.recordmanager.server.miscellaneous.skat;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameter.ParameterType;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;

public class GenerateSkatKeysJobParameterValidator extends DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_INCREMENTAL, ParameterType.LONG, false), //
				param(Constants.JOB_PARAM_FROM_DATE, ParameterType.DATE, false), //
				param(Constants.JOB_PARAM_UNTIL_DATE, ParameterType.DATE, false)
				);
	}


}
