package cz.mzk.recordmanager.server.springbatch;

import java.util.Map;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

public abstract class DefaultJobParametersValidator implements
		IntrospectiveJobParametersValidator {

	@Override
	public void validate(JobParameters parameters)
			throws JobParametersInvalidException {
		for (JobParameterDeclaration decl : getParameters()) {
			Map<String, JobParameter> params = parameters.getParameters();
			JobParameter jobParameter = params.get(decl.getName());
			validate(decl, jobParameter);
		}
	}

	private void validate(JobParameterDeclaration decl,
			JobParameter jobParameter) throws JobParametersInvalidException {
		if (decl.isRequired() && jobParameter == null) {
			throw new JobParametersInvalidException(String.format(
					"Job parameter %s of type %s is required", decl.getName(),
					decl.getType()));
		}
		if (jobParameter != null
				&& !jobParameter.getType().equals(decl.getType())) {
			throw new JobParametersInvalidException(
					String.format(
							"Expected parameter of type %s, got %s for job parameter %s.",
							decl.getType(), jobParameter.getType(),
							decl.getName()));
		}
	}

}
