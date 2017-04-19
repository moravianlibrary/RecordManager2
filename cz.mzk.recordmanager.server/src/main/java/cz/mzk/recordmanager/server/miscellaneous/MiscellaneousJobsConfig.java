package cz.mzk.recordmanager.server.miscellaneous;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cz.mzk.recordmanager.server.index.HarvestedRecordRowMapper;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.miscellaneous.skat.GenerateSkatKeysJobParameterValidator;
import cz.mzk.recordmanager.server.miscellaneous.skat.GenerateSkatKeysProcessor;
import cz.mzk.recordmanager.server.miscellaneous.skat.GenerateSkatKeysWriter;
import cz.mzk.recordmanager.server.miscellaneous.skat.SkatKeysMergedIdsUpdateTasklet;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class MiscellaneousJobsConfig {
	
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private HarvestedRecordRowMapper harvestedRecordRowMapper;
	
	@Autowired
	private SkatKeyDAO skatKeysDao;
	
	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;
	
	@Value(value = "${recordmanager.threadPoolSize:#{1}}")
	private int threadPoolSize = 1;

	@Bean
	public Job generateSkatKeysJob(
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep") Step generateSkatKeysStep,
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":updateStatMergedIdsStep") Step updateStatMergedIdsStep
			) {
		return jobs.get(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS)
				.validator(new GenerateSkatKeysJobParameterValidator())
				.start(generateSkatKeysStep)
				.next(updateStatMergedIdsStep)
				.build();
	}

	@Bean
	public Job generateLocalSkatDedupKeysJob(
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep") Step generateSkatKeysStep) {
		return jobs.get(Constants.JOB_ID_GENERATE_LOCAL_SKAT_DEDUP_KEYS)
				.validator(new GenerateSkatKeysJobParameterValidator())
				.start(generateSkatKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":updateStatMergedIdsStep")
	public Step updateStatMergedIdsStep() throws Exception {
		return steps.get("updateStatMergedIdsStep")
				.tasklet(updateStatMergedIdsStepTasklet(DATE_OVERRIDEN_BY_EXPRESSION))
				.listener(new StepProgressListener())
				.build();
	}
	
	
	
	@Bean(name = "updateStatMergedIdsStep:updateStatMergedIdsTasklet")
	@StepScope
	public Tasklet updateStatMergedIdsStepTasklet(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date fromDate) {
		return new SkatKeysMergedIdsUpdateTasklet(fromDate);
	}
	
	
	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep")
	public Step generateSkatKeysStep() throws Exception {
		return steps.get("generateSkatKeysStep")
				.listener(new StepProgressListener())
				.<Long, Set<SkatKey>> chunk(1000)
				.reader(generateSkatKeysReader(DATE_OVERRIDEN_BY_EXPRESSION))
				.processor(generateSkatKeysProcessor())
				.writer(generateSkatKeysWriter())
				.taskExecutor((TaskExecutor) poolTaskExecutor())
				.build();
	}
	
	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysReader")
	@StepScope
	public ItemReader<Long> generateSkatKeysReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date fromDate)
			throws Exception {
		Date from = fromDate == null ? new Date(0) : fromDate;
		
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id and updated > :updated_time");
		pqpf.setSortKey("id");
		Map<String, Object> parameterValues = new HashMap<String, Object>();
		parameterValues.put("conf_id", Constants.IMPORT_CONF_ID_CASLIN);
		parameterValues.put("updated_time", from);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}
	
	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysProcessor")
	@StepScope
	public GenerateSkatKeysProcessor generateSkatKeysProcessor() {
		return new GenerateSkatKeysProcessor();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysWriter")
	@StepScope
	public GenerateSkatKeysWriter generateSkatKeysWriter() {
		return new GenerateSkatKeysWriter();
	}

	@Bean(name = "threadPoolTaskExecutor")
	public Executor poolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(threadPoolSize);
		executor.setMaxPoolSize(threadPoolSize);
		executor.initialize();
		return executor;
	}

}
