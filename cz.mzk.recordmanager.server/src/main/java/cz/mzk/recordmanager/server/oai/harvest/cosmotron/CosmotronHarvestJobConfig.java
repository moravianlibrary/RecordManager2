package cz.mzk.recordmanager.server.oai.harvest.cosmotron;

import cz.mzk.recordmanager.server.export.HarvestedRecordIdRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.harvest.AsyncOAIItemReader;
import cz.mzk.recordmanager.server.oai.harvest.OAIItemProcessor;
import cz.mzk.recordmanager.server.oai.harvest.OAIItemReader;
import cz.mzk.recordmanager.server.oai.harvest.ReharvestJobExecutionDecider;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CosmotronHarvestJobConfig {

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final int PAGE_SIZE = 1000;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Value(value = "${oai_harvest.async_reader:#{false}}")
	private boolean asyncReader = false;

	//
	@Bean
	public Job cosmotronHarvestJob(
			@Qualifier(Constants.JOB_ID_HARVEST_COSMOTRON + ":cosmotronHarvestStep") Step cosmotronHarvestStep,
			@Qualifier(Constants.JOB_ID_HARVEST_COSMOTRON + ":update996Step") Step update996Step,
			@Qualifier(Constants.JOB_ID_HARVEST_COSMOTRON + ":afterCosmotronHarvestStep") Step afterCosmotronHarvestStep) {
		return jobs.get(Constants.JOB_ID_HARVEST_COSMOTRON) //
				.validator(new CosmotronHarvestJobParametersValidator()) //
				.listener(JobFailureListener.INSTANCE)
				.start(cosmotronHarvestStep) //
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(afterCosmotronHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).to(update996Step)
				.from(afterCosmotronHarvestStep).on(FlowExecutionStatus.COMPLETED.toString()).to(update996Step) //
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":cosmotronHarvestStep")
	public Step harvestStep() {
		ItemReader<List<OAIRecord>> reader;
		if (this.asyncReader) {
			reader = asyncReader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION,
					STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION);
		} else {
			reader = reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION,
					STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION);
		}
		return steps.get("cosmotronHarvestStep") //
				.listener(new StepProgressListener())
				.<List<OAIRecord>, List<HarvestedRecord>>chunk(1) //
				.reader(reader) //
				.processor(oaiItemProcessor())
				.writer(cosmotronRecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST + ":reader")
	@StepScope
	public OAIItemReader reader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
					+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE + ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_RESUMPTION_TOKEN + ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_RESUMPTION_TOKEN + "]}") String resumptionToken,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_REHARVEST + ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_REHARVEST + "]}") String reharvest) {
		return new OAIItemReader(configId, from, to, resumptionToken, reharvest != null && reharvest.equals("true"));
	}

	@Bean(name = "oaiHarvestJob:asyncReader")
	@StepScope
	public AsyncOAIItemReader asyncReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
					+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE + ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_RESUMPTION_TOKEN + ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_RESUMPTION_TOKEN + "]}") String resumptionToken,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_REHARVEST + ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_REHARVEST + "]}") String reharvest) {
		return new AsyncOAIItemReader(configId, from, to, resumptionToken, reharvest != null && reharvest.equals("true"));
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":cosmotronRecordsProcessor")
	@StepScope
	public OAIItemProcessor oaiItemProcessor() {
		return new OAIItemProcessor();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":cosmotronRecordsWriter")
	@StepScope
	public CosmotronRecordWriter cosmotronRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId) {
		return new CosmotronRecordWriter(configId);
	}

	//
	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":update996Step")
	public Step update996Step() throws Exception {
		return steps.get("update996Step") //
				.listener(new StepProgressListener())
				.<HarvestedRecordUniqueId, HarvestedRecordUniqueId>chunk(1) //
				.reader(upate996Reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
				.writer(cosmotronUpdate996Writer()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":update996reader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> upate996Reader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
					+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE + "]}") Date from) throws Exception {

		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		Map<String, Object> parameterValues = new HashMap<>();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT harvested_record_id, import_conf_id, record_id");
		pqpf.setFromClause("FROM cosmotron_periodicals_last_update");
		String where = "WHERE import_conf_id = :conf_id";
		parameterValues.put("conf_id", configId);
		if (from != null) {
			where += " AND last_update > :from";
			parameterValues.put("from", from);
		}
		pqpf.setWhereClause(where);
		pqpf.setSortKey("harvested_record_id");
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(PAGE_SIZE);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.setSaveState(true);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":cosmotronUpdate996Writer")
	@StepScope
	public CosmotronUpdate996Writer cosmotronUpdate996Writer() {
		return new CosmotronUpdate996Writer();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":afterCosmotronHarvestStep")
	public Step afterCosmotronHarvestStep() {
		return steps.get(Constants.JOB_ID_HARVEST_COSMOTRON + ":afterCosmotronHarvestStep") //
				.listener(new StepProgressListener())
				.tasklet(afterCosmotronHarvestTasklet()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":afterCosmotronHarvestTasklet")
	@StepScope
	public Tasklet afterCosmotronHarvestTasklet() {
		return new AfterCosmotronHarvestTasklet();
	}
}
