package cz.mzk.recordmanager.server.imports.antikvariaty;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter.ParameterType;

import java.util.Arrays;
import java.util.Collection;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class AntikvariatyImportJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_CONF_ID, ParameterType.LONG, true),
				param(Constants.JOB_PARAM_IN_FILE, ParameterType.STRING, false),
				param(Constants.JOB_PARAM_REHARVEST, ParameterType.STRING, false)
		);
	}

}
