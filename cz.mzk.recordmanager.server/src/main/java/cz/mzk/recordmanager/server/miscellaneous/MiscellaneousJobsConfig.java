package cz.mzk.recordmanager.server.miscellaneous;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import cz.mzk.recordmanager.server.index.HarvestedRecordRowMapper;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.miscellaneous.skat.GenerateSkatKeysJobParameterValidator;
import cz.mzk.recordmanager.server.miscellaneous.skat.GenerateSkatKeysProcessor;
import cz.mzk.recordmanager.server.miscellaneous.skat.GenerateSkatKeysWriter;
import cz.mzk.recordmanager.server.miscellaneous.skat.SkatKeysMergedIdsUpdateTasklet;
import cz.mzk.recordmanager.server.model.SkatKey;
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
	
	private static final int SKAT_IMPORT_CONF_ID = 316;
	
	// 2000-01-01 00:00:00
	private Date generateSkatKeysStartDate = new Date(0);
	
	@Bean
	public Job generateSkatKeysJob(
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateFromDateStep") Step generateFromDateStep,
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep") Step generateSkatKeysStep,
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":updateStatMergedIdsStep") Step updateStatMergedIdsStep
			) {
		return jobs.get(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS)
				.validator(new GenerateSkatKeysJobParameterValidator())
				.start(generateFromDateStep)
				.next(generateSkatKeysStep)
				.next(updateStatMergedIdsStep)
				.build();
	}
	
	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateFromDateStep")
	public Step generateSkatKeysInitStep() {
		return steps.get("generateFromDateStep")
				.tasklet(generateSkatKeysInitStepTasklet(null))
				.listener(new StepProgressListener())
				.build();
	}
	
	@Bean(name = "generateSkatKeysInitStep:generateSkatKeysInitStepTasklet")
	@StepScope
	public Tasklet generateSkatKeysInitStepTasklet(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_INCREMENTAL + "]}") Long increamental) {
		return new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution,
					ChunkContext chunkContext) throws Exception {

				if (increamental == null || increamental.equals(0L)) {
					return RepeatStatus.FINISHED;
					
				}
				Session session = sessionFactory.getCurrentSession();
				Timestamp timestamp = (Timestamp) session
						.createSQLQuery(
								String.format("select max(bje.create_time) "
										+ "from batch_job_instance bji inner join "
										+ "batch_job_execution bje on bji.job_instance_id = bje.job_instance_id "
										+ "where bji.job_name = '%s'",Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS))
						.setMaxResults(1)
						.uniqueResult();
				
				if (timestamp != null) {
					generateSkatKeysStartDate = timestamp;
				}
				return RepeatStatus.FINISHED;
			}
		};
	}
	
	
	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":updateStatMergedIdsStep")
	public Step updateStatMergedIdsStep() throws Exception {
		return steps.get("updateStatMergedIdsStep")
				.tasklet(updateStatMergedIdsStepTasklet())
				.listener(new StepProgressListener())
				.build();
	}
	
	
	
	@Bean(name = "updateStatMergedIdsStep:updateStatMergedIdsTasklet")
	@StepScope
	public Tasklet updateStatMergedIdsStepTasklet() {
		return new SkatKeysMergedIdsUpdateTasklet(generateSkatKeysStartDate);
	}
	
	
	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep")
	public Step generateSkatKeysStep() throws Exception {
		return steps.get("generateSkatKeysStep")
				.listener(new StepProgressListener())
				.<Long, Set<SkatKey>> chunk(1000)
				.reader(generateSkatKeysReader())
				.processor(generateSkatKeysProcessor())
				.writer(generateSkatKeysWriter())
				.build();
	}
	
	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysReader")
	@StepScope
	public ItemReader<Long> generateSkatKeysReader()
			throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id and updated > :updated_time");
		pqpf.setSortKey("id");
		Map<String, Object> parameterValues = new HashMap<String, Object>();
		parameterValues.put("conf_id", SKAT_IMPORT_CONF_ID);	
		parameterValues.put("updated_time", generateSkatKeysStartDate);
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

}
