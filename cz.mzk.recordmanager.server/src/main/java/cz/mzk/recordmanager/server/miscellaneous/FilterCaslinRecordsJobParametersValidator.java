package cz.mzk.recordmanager.server.miscellaneous;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;

public class FilterCaslinRecordsJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public void validate(JobParameters parameters)
			throws JobParametersInvalidException {
		super.validate(parameters);
	}

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList();
	}

}
