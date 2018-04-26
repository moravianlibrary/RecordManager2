package cz.mzk.recordmanager.server.imports.inspirations;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter.ParameterType;

import java.util.Collection;
import java.util.Collections;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class InspirationImportJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Collections.singletonList(
				param(Constants.JOB_PARAM_IN_FILE, ParameterType.STRING, false)//
		);
	}

}
