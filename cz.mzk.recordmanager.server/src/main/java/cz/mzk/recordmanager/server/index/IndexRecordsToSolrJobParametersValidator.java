package cz.mzk.recordmanager.server.index;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.batch.core.JobParameter.ParameterType;

import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;

public class IndexRecordsToSolrJobParametersValidator extends
DefaultJobParametersValidator {

	@Override
	public Collection<JobParameterDeclaration> getParameters() {
		return Arrays.asList(
				param(IndexRecordsToSolrJobConfig.DATE_FROM_JOB_PARAM, ParameterType.DATE, false), //
				param(IndexRecordsToSolrJobConfig.DATE_TO_JOB_PARAM, ParameterType.DATE, false), //
				param(IndexRecordsToSolrJobConfig.SOLR_URL_JOB_PARAM, ParameterType.STRING, true) //
				);
	}

}
