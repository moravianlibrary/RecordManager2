package cz.mzk.recordmanager.server.dedup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import cz.mzk.recordmanager.server.springbatch.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import com.google.common.io.CharStreams;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
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

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	private String dropOldDedupKeysSql = CharStreams.toString(new InputStreamReader(getClass() //
			.getClassLoader().getResourceAsStream("job/regenerateDedupKeysJob/dropDedupKeys.sql"), "UTF-8"));

	public RegenerateDedupKeysJobConfig() throws IOException {
	}

	// regenerate
	@Bean
	public Job RegenerateDedupKeysJob(
			@Qualifier(Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenerateDedupKeysStep") Step regenDedupKeysStep) {
		return jobs.get(Constants.JOB_ID_REGEN_DEDUP_KEYS)
				.validator(new RegenerateJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.start(regenDedupKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenerateDedupKeysStep")
	public Step regenerateDedupKeysStep() throws Exception {
		return steps.get("regenerateDedupKeysStep")
				.listener(new StepProgressListener())
				.<Long, Long>chunk(100)//
				.reader(reader(LONG_OVERRIDEN_BY_EXPRESSION))//
				.writer(writer()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenerateDedupKeysReader")
	@StepScope
	public ItemReader<Long> reader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_ID + "]}") Long startRecordId
	) throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM tmp_ids_celitebib hr");
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	// missing
	@Bean
	public Job RegenerateMissingDedupKeysJob(
			@Qualifier(Constants.JOB_ID_REGEN_MISSING_DEDUP_KEYS + ":regenerateMissingDedupKeysStep") Step regenerateMissingDedupKeysStep) {
		return jobs.get(Constants.JOB_ID_REGEN_MISSING_DEDUP_KEYS)
				.validator(new RegenerateDedupKeysJobParameters())
				.listener(JobFailureListener.INSTANCE)
				.start(regenerateMissingDedupKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGEN_MISSING_DEDUP_KEYS + ":regenerateMissingDedupKeysStep")
	public Step regenerateMissingDedupKeysStep() throws Exception {
		return steps.get("regenerateMissingDedupKeysStep")
				.<Long, Long>chunk(100)//
				.reader(readerMissing())//
				.writer(writer()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGEN_MISSING_DEDUP_KEYS + ":regenerateMissingDedupKeysReader")
	@StepScope
	public ItemReader<Long> readerMissing() throws Exception {
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

	// drop keys
	@Bean
	public Job DropDedupKeysJob(
			@Qualifier(Constants.JOB_ID_DROP_DEDUP_KEYS + ":dropOldDedupKeysStep") Step dropOldDedupKeysStep) {
		return jobs.get(Constants.JOB_ID_DROP_DEDUP_KEYS)
				.validator(new RegenerateDedupKeysJobParameters())
				.listener(JobFailureListener.INSTANCE)
				.start(dropOldDedupKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_DROP_DEDUP_KEYS + ":dropOldDedupKeysStep")
	public Step dropOldDedupKeysStep() {
		return steps.get("dropOldDedupKeysStep")
				.tasklet(dropOldDedupKeysTasklet())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DROP_DEDUP_KEYS + ":dropOldDedupKeysTasklet")
	@StepScope
	public Tasklet dropOldDedupKeysTasklet() {
		return new SqlCommandTasklet(dropOldDedupKeysSql.split(";"));
	}

	// writer
	@Bean(name = Constants.JOB_ID_REGEN_DEDUP_KEYS + ":regenerateDedupKeysWriter")
	@StepScope
	public ItemWriter<Long> writer() throws Exception {
		return new RegenerateDedupKeysWriter();
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

