package cz.mzk.recordmanager.server.adresar;

import java.util.Collection;
import java.util.Collections;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;

public class AdresarHarvestJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Collections.emptyList();
	}

}
