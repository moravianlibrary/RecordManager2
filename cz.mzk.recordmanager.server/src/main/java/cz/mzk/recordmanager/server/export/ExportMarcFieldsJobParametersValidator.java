package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.JobParameter.ParameterType;

import java.util.Arrays;
import java.util.Collection;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

public class ExportMarcFieldsJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_CONF_ID, ParameterType.STRING, false), //
				param(Constants.JOB_PARAM_FORMAT, ParameterType.STRING, true),
				param(Constants.JOB_PARAM_OUT_FILE, ParameterType.STRING, true), //
				param(Constants.JOB_PARAM_FIELDS, ParameterType.STRING, true)
		);
	}

}
