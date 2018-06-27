package cz.mzk.recordmanager.server.miscellaneous.caslin.filter;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

import java.util.Collection;
import java.util.Collections;

public class FilterCaslinRecordsJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public void validate(JobParameters parameters)
			throws JobParametersInvalidException {
		super.validate(parameters);
	}

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Collections.emptyList();
	}

}
