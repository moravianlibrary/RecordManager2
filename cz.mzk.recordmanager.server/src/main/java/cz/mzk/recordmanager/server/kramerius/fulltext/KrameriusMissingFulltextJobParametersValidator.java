package cz.mzk.recordmanager.server.kramerius.fulltext;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameter.ParameterType;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;

public class KrameriusMissingFulltextJobParametersValidator extends
	DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(Constants.JOB_PARAM_CONF_ID, ParameterType.LONG,true), //
				param(Constants.JOB_PARAM_FROM_DATE, ParameterType.DATE, false), //
				param(Constants.JOB_PARAM_UNTIL_DATE, ParameterType.DATE, false), //
				param(Constants.JOB_PARAM_FULLTEXT_FIRST, ParameterType.STRING, false), //
				param(Constants.JOB_PARAM_FULLTEXT_LAST, ParameterType.STRING, false) //
			);
	}

}
