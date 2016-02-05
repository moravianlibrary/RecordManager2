package cz.mzk.recordmanager.server.index;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.apache.solr.common.SolrInputDocument;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.jdbc.DedupRecordRowMapper;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.jdbc.StringValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.springbatch.DelegatingHibernateProcessor;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class IndexRecordsToSolrJobConfig {

	private static Logger logger = LoggerFactory.getLogger(IndexRecordsToSolrJobConfig.class);

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final int CHUNK_SIZE = 1;

	private static final int PAGE_SIZE = 5000;

	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private HarvestedRecordRowMapper harvestedRecordRowMapper;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TaskExecutor taskExecutor;

	@Bean
	public Job indexAllRecordsToSolrJob(
			@Qualifier("indexRecordsToSolrJob:updateRecordsStep") Step updateRecordsStep,
			@Qualifier("indexRecordsToSolrJob:deleteOrphanedRecordsStep") Step deleteOrphanedRecordsStep) {
		return jobs.get(Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS)
				.validator(new IndexRecordsToSolrJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(updateRecordsStep)
				.next(deleteOrphanedRecordsStep)
				.end()
				.build();
	}

	@Bean
	public Job indexIndividualRecordsToSolrJob(@Qualifier("indexRecordsToSolrJob:indexIndividualRecordsStep") Step step) {
		return jobs.get(Constants.JOB_ID_SOLR_INDEX_INDIVIDUAL_RECORDS)
				.validator(new IndexIndividualRecordsToSolrJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(step)
				.end()
				.build();
	}

	@Bean(name="indexRecordsToSolrJob:updateRecordsStep")
	public Step updateRecordsStep() throws Exception {
		return steps.get("updateRecordsJobStep")
			.<DedupRecord, Future<List<SolrInputDocument>>> chunk(CHUNK_SIZE) //
			.reader(updatedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
			.processor(asyncUpdatedRecordsProcessor()) //
			.writer(updatedRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
			.build();
	}

	@Bean(name="indexRecordsToSolrJob:deleteOrphanedRecordsStep")
	public Step deleteOrphanedRecordsStep() throws Exception {
		return steps.get("deleteOrphanedRecordsJobStep")
			.<Long, Long> chunk(20) //
			.reader(orphanedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
			.writer(orphanedRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
			.build();
	}

	@Bean(name="indexRecordsToSolrJob:indexIndividualRecordsStep")
	public Step indexIndividualRecordsStep() throws Exception {
		return steps.get("indexIndividualRecordsStep")
				.tasklet(indexIndividualRecordsTasklet(STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name="indexRecordsToSolrJob:indexIndividualRecordsTasklet")
	@StepScope
	public Tasklet indexIndividualRecordsTasklet(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String serverUrl,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_IDS + "]}") String recordIds) throws Exception {
		List<String> records = Arrays.asList(recordIds.split(","));
		return new IndexIndividualRecordsTasklet(serverUrl, records);
	}

	@Bean(name = "indexRecordsToSolrJob:updatedRecordsReader")
	@StepScope
	public JdbcPagingItemReader<DedupRecord> updatedRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to)
			throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT dedup_record_id");
		pqpf.setFromClause("FROM dedup_record_last_update");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE last_update BETWEEN :from AND :to");
		}
		pqpf.setSortKey("dedup_record_id");
		JdbcPagingItemReader<DedupRecord> reader = new JdbcPagingItemReader<DedupRecord>();
		reader.setRowMapper(new DedupRecordRowMapper("dedup_record_id"));
		reader.setPageSize(PAGE_SIZE);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		if (from != null && to != null) {
			Map<String, Object> parameterValues = new HashMap<String, Object>();
			parameterValues.put("from", from);
			parameterValues.put("to", to);
			reader.setParameterValues(parameterValues);
		}
		reader.setSaveState(true);
		reader.afterPropertiesSet();
		return reader;
	}

    @Bean(name="indexRecordsToSolrJob:updatedRecordsProcessor")
	@StepScope
	public SolrRecordProcessor updatedRecordsProcessor() {
		return new SolrRecordProcessor();
	}

	@Bean(name = "indexRecordsToSolrJob:asyncUpdatedRecordsProcessor")
	@StepScope
	public AsyncItemProcessor<DedupRecord, List<SolrInputDocument>> asyncUpdatedRecordsProcessor() {
		AsyncItemProcessor<DedupRecord, List<SolrInputDocument>> processor = new AsyncItemProcessor<>();
		processor.setDelegate(new DelegatingHibernateProcessor<>(sessionFactory, updatedRecordsProcessor()));
		processor.setTaskExecutor(taskExecutor);
		return processor;
	}

    @Bean(name="indexRecordsToSolrJob:updatedRecordsWriter")
    @StepScope
    public SolrIndexWriter updatedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
    	return new SolrIndexWriter(solrUrl);
    }


	@Bean(name="indexRecordsToSolrJob:orphanedRecordsReader")
	@StepScope
	public JdbcPagingItemReader<Long> orphanedRecordsReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE  + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to) throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT dedup_record_id");
		pqpf.setFromClause("FROM dedup_record_orphaned");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE orphaned BETWEEN :from AND :to");
		}
		pqpf.setSortKey("dedup_record_id");
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<Long>();
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		if (from != null && to != null) {
			Map<String, Object> parameterValues = new HashMap<String, Object>();
    		parameterValues.put("from", from);
    		parameterValues.put("to", to);
    		reader.setParameterValues(parameterValues);
    	}
		reader.setSaveState(true);
		reader.afterPropertiesSet();
		return reader;
    }

	@Bean(name="indexRecordsToSolrJob:orphanedRecordsWriter")
	@StepScope
	public OrphanedDedupRecordsWriter orphanedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
		return new OrphanedDedupRecordsWriter(solrUrl);
	}
    
	@Bean(name = "indexRecordsToSolrJob:orphanedRecordsReader")
	@StepScope
	public JdbcPagingItemReader<String> orphanedHarvestedRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to)
			throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		JdbcPagingItemReader<String> reader = new JdbcPagingItemReader<String>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id, hc.id_prefix || '.' || hr.record_id solr_id");
		pqpf.setFromClause("FROM harvested_record hr JOIN import_conf ic ON hr.import_conf_id = ic.id");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE hr.deleted BETWEEN :from AND :to");
		} else {
			pqpf.setWhereClause("WHERE hr.deleted IS NOT NULL");
		}
		pqpf.setSortKeys(ImmutableMap.of("import_conf_id", Order.ASCENDING,
				"record_id", Order.ASCENDING));
		reader.setRowMapper(new StringValueRowMapper(3));
		reader.setPageSize(PAGE_SIZE);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		if (from != null && to != null) {
			Map<String, Object> parameterValues = new HashMap<String, Object>();
			parameterValues.put("from", from);
			parameterValues.put("to", to);
			reader.setParameterValues(parameterValues);
		}
		reader.setSaveState(true);
		reader.afterPropertiesSet();
		return reader;
	}

}
