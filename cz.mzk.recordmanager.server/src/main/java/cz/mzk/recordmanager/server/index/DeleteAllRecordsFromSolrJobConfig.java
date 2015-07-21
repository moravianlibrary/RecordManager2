package cz.mzk.recordmanager.server.index;

import static cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration.param;

import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrServer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.JobParameter.ParameterType;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.springbatch.DefaultJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class DeleteAllRecordsFromSolrJobConfig {

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final String ALL_DOCUMENTS_QUERY = "*:*";

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private SolrServerFactory factory;

	@Autowired
	private StepBuilderFactory steps;

	public static class DeleteAllRecordsFromSolrJobParametersValidator extends DefaultJobParametersValidator {

		@Override
		public Collection<JobParameterDeclaration> getParameters() {
			return Arrays.asList(
					param(Constants.JOB_PARAM_SOLR_URL, ParameterType.STRING, true), //
					param(Constants.JOB_PARAM_SOLR_QUERY, ParameterType.STRING, false) //
			);
		}

	}

	@Bean
	public Job deleteAllRecordsFromSolrJob(@Qualifier("deleteAllRecordsFromSolrJob:deleteStep") Step deleteStep) {
		return jobs.get(Constants.JOB_ID_DELETE_ALL_RECORDS_FROM_SOLR) //
				.validator(new DeleteAllRecordsFromSolrJobParametersValidator()) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(deleteStep) //
				.end() //
				.build();
	}

	@Bean(name="deleteAllRecordsFromSolrJob:deleteStep")
	public Step updateHarvestedRecordsStep() throws Exception {
		return steps.get("deleteStep") //
				.tasklet(deleteTasklet(STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = "deleteAllRecordsFromSolrJob:deleteTasklet")
	@StepScope
	public Tasklet deleteTasklet(
			final @Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl,
			final @Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_QUERY + "]}") String solrQuery) {
		final String query = (solrQuery != null) ? solrQuery : ALL_DOCUMENTS_QUERY;
		return new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution,
					ChunkContext chunkContext) throws Exception {
				SolrServer server = factory.create(solrUrl);
				server.deleteByQuery(query);
				server.commit();
				return RepeatStatus.FINISHED;
			}
		};
	}

}
