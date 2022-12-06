package cz.mzk.recordmanager.server.imports.inspirations;

import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class InspirationImportJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job inspirationImportJob(
			@Qualifier(Constants.JOB_ID_IMPORT_INSPIRATION + ":importInspirationStep") Step inspirationImportStep,
			@Qualifier(Constants.JOB_ID_IMPORT_INSPIRATION + ":inspirationCleanStep") Step inspirationCleanStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_INSPIRATION)
				.validator(new InspirationImportJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.start(inspirationImportStep)
				.next(inspirationCleanStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_INSPIRATION + ":importInspirationStep")
	public Step inspirationImportStep() throws Exception {
		return steps.get("importInspirationStep")
				.listener(new StepProgressListener())
				.<Map<String, List<String>>, Map<String, List<String>>>chunk(1)//
				.reader(inspirationFileReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(InspirationImportWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_INSPIRATION + ":importInspirationReader")
	@StepScope
	public InspirationImportFileReader inspirationFileReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
		return new InspirationImportFileReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_INSPIRATION + ":writer")
	@StepScope
	public InspirationImportWriter InspirationImportWriter() {
		return new InspirationImportWriter();
	}

	@Bean
	public Job inspirationDeleteJob(
			@Qualifier(Constants.JOB_ID_DELETE_INSPIRATION + ":deleteInspirationStep") Step inspirationDeleteStep) {
		return jobs.get(Constants.JOB_ID_DELETE_INSPIRATION)
				.validator(new InspirationDeleteJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(inspirationDeleteStep)
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_DELETE_INSPIRATION + ":deleteInspirationStep")
	public Step inspirationDeleteStep() throws Exception {
		return steps.get("deleteInspirationStep")
				.listener(new StepProgressListener())
				.<Long, Long>chunk(1)//
				.reader(inspirationDeleteReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(InspirationDeleteWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_DELETE_INSPIRATION + ":deleteInspirationReader")
	@StepScope
	public ItemReader<Long> inspirationDeleteReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_DELETE_INSPIRATION + "]}") String name) throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record_inspiration");
		pqpf.setWhereClause("WHERE inspiration_id in (select id from inspiration where name = :name)");
		pqpf.setSortKey("id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("name", name);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new InspirationIdRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_DELETE_INSPIRATION + ":writer")
	@StepScope
	public InspirationDeleteWriter InspirationDeleteWriter() {
		return new InspirationDeleteWriter();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_INSPIRATION + ":inspirationCleanStep")
	public Step inspirationCleanStep() {
		return steps.get("inspirationCleanStep") //
				.listener(new StepProgressListener())
				.tasklet(inspirationCleanTasklet()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_INSPIRATION + ":inspirationCleanTasklet")
	@StepScope
	public Tasklet inspirationCleanTasklet() {
		return new InspirationCleanTasklet(InspirationType.INSPIRATION);
	}

}

