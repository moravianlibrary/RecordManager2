package cz.mzk.recordmanager.server.imports;

import java.util.Collection;
import java.util.Collections;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;

public class ObalkyKnihHarvestJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Collections.emptyList();
	}

}
