package cz.mzk.recordmanager.server.miscellaneous.agrovoc;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameter.ParameterType;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;

public class AgrovocConvertorJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_IN_FILE, ParameterType.STRING, true), //
				param(Constants.JOB_PARAM_OUT_FILE, ParameterType.STRING, true)
				);
	}

}
