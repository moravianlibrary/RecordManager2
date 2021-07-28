package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.dedup.RegenerateJobParametersValidator;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.harvest.ReharvestJobExecutionDecider;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
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

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KramAvailabilityJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job HarvestKramAvailabilityJob(
			@Qualifier(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":harvestStep") Step harvestStep,
			@Qualifier(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":afterHarvestStep") Step afterHarvestStep) {
		return jobs.get(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY)
				.validator(new KramAvailabilityJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(harvestStep)
				.next(ReharvestJobExecutionDecider.INSTANCE)
				.on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString())
				.to(afterHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE)
				.on(FlowExecutionStatus.COMPLETED.toString())
				.end() //
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":harvestStep")
	public Step harvestKramAvailabilityStep() throws Exception {
		return steps.get(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":harvestStep")
				.listener(new StepProgressListener())
				.<List<KramAvailability>, List<KramAvailability>>chunk(10)//
				.faultTolerant()
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(harvestKramAvailabilityReader(LONG_OVERRIDEN_BY_EXPRESSION))//
				.writer(harvestKramAvailabilityWriter()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":reader")
	@StepScope
	public KramAvailabilityReader harvestKramAvailabilityReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId) {
		return new KramAvailabilityReader(configId, "title");
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":writer")
	@StepScope
	public ItemWriter<List<KramAvailability>> harvestKramAvailabilityWriter() {
		return new KramAvailabilityWriter();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":afterHarvestStep")
	public Step afterHarvestStep() {
		return steps.get("afterHarvestStep") //
				.tasklet(afterHarvestTasklet()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":afterHarvestTasklet")
	@StepScope
	public Tasklet afterHarvestTasklet() {
		return new AfterHarvestAvailabilityTasklet();
	}

	// regenerate dedup key
	@Bean
	public Job RegenerateKramAvailabilityJob(
			@Qualifier(Constants.JOB_ID_REGENERATE_KRAM_AVAILABILITY + ":regenerateKramAvailabilityStep") Step regenerateKramAvailabilityStep) {
		return jobs.get(Constants.JOB_ID_REGENERATE_KRAM_AVAILABILITY)
				.validator(new RegenerateJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.start(regenerateKramAvailabilityStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGENERATE_KRAM_AVAILABILITY + ":regenerateKramAvailabilityStep")
	public Step regenerateKramAvailabilityStep() throws Exception {
		return steps.get("regenerateKramAvailabilityStep")
				.listener(new StepProgressListener())
				.<Long, Long>chunk(10)//
				.reader(reader(LONG_OVERRIDEN_BY_EXPRESSION))//
				.writer(writer()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGENERATE_KRAM_AVAILABILITY + ":regenerateKramAvailabilityReader")
	@StepScope
	public ItemReader<Long> reader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_ID + "]}") Long startRecordId
	) throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM kram_availability hr");
		if (startRecordId != null) {
			pqpf.setWhereClause("WHERE id > :startId");
			Map<String, Object> parameterValues = new HashMap<>();
			parameterValues.put("startId", startRecordId);
			reader.setParameterValues(parameterValues);
		}
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_REGENERATE_KRAM_AVAILABILITY + ":regenerateKramAvailabilityWriter")
	@StepScope
	public ItemWriter<Long> writer() throws Exception {
		return new RegenerateKramAvailabilityWriter();
	}

}
