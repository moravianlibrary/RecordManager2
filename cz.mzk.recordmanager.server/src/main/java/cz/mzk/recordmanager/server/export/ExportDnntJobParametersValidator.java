package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter.ParameterType;

import java.util.Arrays;
import java.util.Collection;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class ExportDnntJobParametersValidator extends DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_OUT_FILE, ParameterType.STRING, true) //
		);
	}

}
