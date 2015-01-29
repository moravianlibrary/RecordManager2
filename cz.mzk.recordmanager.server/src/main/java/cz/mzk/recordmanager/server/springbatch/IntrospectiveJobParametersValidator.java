package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;

import org.springframework.batch.core.JobParametersValidator;

public interface IntrospectiveJobParametersValidator extends JobParametersValidator {

	public Collection<JobParameterDeclaration> getParameters();
	
}
