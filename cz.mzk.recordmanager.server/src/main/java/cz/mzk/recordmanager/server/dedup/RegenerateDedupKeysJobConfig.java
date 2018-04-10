package cz.mzk.recordmanager.server.dedup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import com.google.common.io.CharStreams;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.springbatch.IntrospectiveJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class RegenerateDedupKeysJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TaskExecutor taskExecutor;

	private String dropOldDedupKeysSql = CharStreams.toString(new InputStreamReader(getClass() //
			.getClassLoader().getResourceAsStream("job/regenerateDedupKeysJob/dropDedupKeys.sql"), "UTF-8"));

	public RegenerateDedupKeysJobConfig() throws IOException {
	}

	@Bean
	public Job RegenerateDedupKeysJob(
			@Qualifier(Constants.JOB_ID_REGEN_DEDUP_KEYS + ":dropOldDedupKeysStep") Step dropOldDedupKeysStep,
			@Qualifier(Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenarateDedupKeysStep") Step regenDedupKeysStep) {
		return jobs.get(Constants.JOB_ID_REGEN_DEDUP_KEYS)
				.validator(new RegenerateDedupKeysJobParameters())
				.listener(JobFailureListener.INSTANCE)
				.start(dropOldDedupKeysStep)
				.next(regenDedupKeysStep)
				.build();
	}

	@Bean
	public Job RegenerateMissingDedupKeysJob(
			@Qualifier(Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenarateDedupKeysStep") Step regenDedupKeysStep) {
		return jobs.get(Constants.JOB_ID_REGEN_MISSING_DEDUP_KEYS)
				.validator(new RegenerateDedupKeysJobParameters())
				.listener(JobFailureListener.INSTANCE)
				.start(regenDedupKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":dropOldDedupKeysStep")
	public Step prepareTempCnbnTableStep() {
		return steps.get("dropOldDedupKeysStep")
				.tasklet(dropOldDedupKeysTasklet())
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenarateDedupKeysStep")
	public Step regenerateDedupKeysStep() throws Exception {
		return steps.get("regenarateDedupKeysStep")
				.<Long, Long>chunk(100)//
				.reader(reader())//
				.writer(writer()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenarateDedupKeysReader")
	@StepScope
	public ItemReader<Long> reader() throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record hr");
		pqpf.setWhereClause("WHERE dedup_keys_hash is null");
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenarateDedupKeysWriter")
	@StepScope
	public ItemWriter<Long> writer() throws Exception {
		return new RegenerateDedupKeysWriter();
	}

	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":dropOldDedupKeysTasklet")
	@StepScope
	public Tasklet dropOldDedupKeysTasklet() {
		return new SqlCommandTasklet(dropOldDedupKeysSql.split(";"));
	}

	public class RegenerateDedupKeysJobParameters implements IntrospectiveJobParametersValidator {

		@Override
		public void validate(JobParameters parameters)
				throws JobParametersInvalidException {
		}

		@Override
		public Collection<JobParameterDeclaration> getParameters() {
			return Collections.emptyList();

		}
	}
}

