package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.export.HarvestedRecordIdRowMapper;
import cz.mzk.recordmanager.server.jdbc.DedupRecordRowMapper;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;

import javax.sql.DataSource;

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

	@Bean
    public Job CosmotronHarvestJob(
    		@Qualifier(Constants.JOB_ID_HARVEST_COSMOTRON+":cosmoStep") Step cosmoStep) {
        return jobs.get(Constants.JOB_ID_HARVEST_COSMOTRON) //
        		.validator(new CosmotronHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(cosmoStep) //
				.end()
				.build();
    }

    @Bean(name=Constants.JOB_ID_HARVEST_COSMOTRON+":cosmoStep")
    public Step cosmoStep() {
        return steps.get("step1") //
            .<List<OAIRecord>, List<HarvestedRecord>> chunk(1) //
            .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
            .processor(cosmotronItemProcessor(STRING_OVERRIDEN_BY_EXPRESSION))
            .writer(harvestedRecordWriter()) //
            .build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST + ":reader")
	@StepScope
	public OAIItemReader reader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
								@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
    				+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE +"]}") Date from,
								@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE+"]"
    				+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE +"]}") Date to,
								@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_RESUMPTION_TOKEN+"]"
    	    		+ "?:jobParameters[" + Constants.JOB_PARAM_RESUMPTION_TOKEN +"]}") String resumptionToken) {
    	return new OAIItemReader(configId, from, to, resumptionToken);
    }

    @Bean(name=Constants.JOB_ID_HARVEST_COSMOTRON+":HarvestedRecordWriter")
    @StepScope
    public ItemWriter<List<HarvestedRecord>> harvestedRecordWriter() {
    	return new HarvestedRecordWriter();
    }

	@Bean(name=Constants.JOB_ID_HARVEST_COSMOTRON+":processor")
    @StepScope
    public CosmotronItemProcessor cosmotronItemProcessor(
    		@Value("#{jobParameters[" + Constants.JOB_PARAM_DELETED_OUT_FILE + "]}") String deletedOutFile) {
    	return new CosmotronItemProcessor(deletedOutFile);
	}

	//
	@Bean
	public Job newCosmotronHarvestJob(
//			@Qualifier(Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":newCosmoStep") Step newCosmoStep,
			@Qualifier(Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":update996Step") Step update996Step) {
		return jobs.get(Constants.JOB_ID_NEW_HARVEST_COSMOTRON) //
				.validator(new CosmotronHarvestJobParametersValidator()) //
				//.start(newCosmoStep) //
				.start(update996Step)
				.build();
	}

	@Bean(name = Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":newCosmoStep")
	public Step newHarvestStep() {
		return steps.get("newCosmoStep") //
				.listener(new StepProgressListener())
				.<List<OAIRecord>, List<HarvestedRecord>>chunk(1) //
				.reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
				.processor(oaiItemProcessor())
				.writer(newCosmotronRecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":newCosmotronRecordsProcessor")
	@StepScope
	public OAIItemProcessor oaiItemProcessor() {
		return new OAIItemProcessor();
	}

	@Bean(name = Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":newCosmotronRecordsWriter")
	@StepScope
	public newCosmotronRecordWriter newCosmotronRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId) {
		return new newCosmotronRecordWriter(configId);
	}

	//
	@Bean(name = Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":update996Step")
	public Step update996Step() throws Exception {
		return steps.get("update996Step") //
				.listener(new StepProgressListener())
				.<HarvestedRecordUniqueId, HarvestedRecordUniqueId>chunk(1) //
				.reader(upate996Reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
				.writer(cosmotronUpdate996Writer()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":update996reader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> upate996Reader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
					+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE + "]"
					+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to) throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		Map<String, Object> parameterValues = new HashMap<>();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT harvested_record_id, import_conf_id, record_id");
		pqpf.setFromClause("FROM cosmotron_periodicals_last_update");
		String where = "WHERE import_conf_id = :conf_id";
		parameterValues.put("conf_id", configId);
		if (from != null) {
			where += " last_update BETWEEN :from AND :to";
			parameterValues.put("from", from);
			parameterValues.put("to", to);
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

	@Bean(name = Constants.JOB_ID_NEW_HARVEST_COSMOTRON + ":cosmotronUpdate996Writer")
	@StepScope
	public CosmotronUpdate996Writer cosmotronUpdate996Writer() {
		return new CosmotronUpdate996Writer();
	}
}
