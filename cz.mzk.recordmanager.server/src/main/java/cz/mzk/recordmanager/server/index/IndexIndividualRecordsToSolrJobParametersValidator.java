package cz.mzk.recordmanager.server.index;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameter.ParameterType;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;

public class IndexIndividualRecordsToSolrJobParametersValidator extends
		DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays
				.asList(param(Constants.JOB_PARAM_SOLR_URL, ParameterType.STRING, true), //
						param(Constants.JOB_PARAM_RECORD_IDS, ParameterType.STRING, true) //
				);
	}

}
