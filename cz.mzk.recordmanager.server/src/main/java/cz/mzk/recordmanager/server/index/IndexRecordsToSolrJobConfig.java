package cz.mzk.recordmanager.server.index;

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
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.jdbc.DedupRecordRowMapper;
import cz.mzk.recordmanager.server.jdbc.StringValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.DelegatingHibernateProcessor;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class IndexRecordsToSolrJobConfig {

	private static Logger logger = LoggerFactory.getLogger(IndexRecordsToSolrJobConfig.class);

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final Integer INTEGER_OVERRIDEN_BY_EXPRESSION = null;

	private static final int CHUNK_SIZE = 1000;

	private static final int PAGE_SIZE = 5000;
	
	private static final int CONCURRENCY_LIMIT = 8;

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

	@Bean
	public Job indexAllRecordsToSolrJob(
			@Qualifier("indexRecordsToSolrJob:updateRecordsStep") Step updateRecordsStep,
			@Qualifier("indexRecordsToSolrJob:deleteOrphanedRecordsStep") Step deleteOrphanedRecordsStep,
			@Qualifier("indexLocalRecordsToSolrJob:deleteOrphanedHarvestedRecordsStep") Step deleteOrphanedHarvestedRecordsStep,
			@Qualifier("indexLocalRecordsToSolrJob:updateHarvestedRecordsStep") Step updateHarvestedRecordsStep) {
		return jobs.get(Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS)
        		.validator(new IndexRecordsToSolrJobParametersValidator())
        		.listener(JobFailureListener.INSTANCE)
        		.flow(deleteOrphanedHarvestedRecordsStep)
				.next(updateRecordsStep)
				.next(deleteOrphanedRecordsStep)
				.end()
				.build();
	}
	
	@Bean
	public Job indexRecordsToSolrJob(@Qualifier("indexRecordsToSolrJob:updateRecordsStep") Step updateRecordsStep,
			@Qualifier("indexRecordsToSolrJob:deleteOrphanedRecordsStep") Step deleteOrphanedRecordsStep) {
		return jobs.get(Constants.JOB_ID_SOLR_INDEX)
				.validator(new IndexRecordsToSolrJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(updateRecordsStep)
				.next(deleteOrphanedRecordsStep)
				.end()
				.build();
    }
    
    @Bean
    public Job indexLocalRecordsToSolrJob(@Qualifier("indexLocalRecordsToSolrJob:updateHarvestedRecordsStep") Step updateHarvestedRecordsStep) {
        return jobs.get(Constants.JOB_ID_SOLR_INDEX_LOCAL_RECORDS)
        		.validator(new IndexRecordsToSolrJobParametersValidator())
        		.listener(JobFailureListener.INSTANCE)
				.flow(updateHarvestedRecordsStep)
				.end()
				.build();
    }

    // dedup records
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
			.<String, String> chunk(20) //
			.reader(orphanedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
			.writer(orphanedRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
			.build();
	}

	@Bean(name = "indexRecordsToSolrJob:updatedRecordsReader")
	@StepScope
	public ItemReader<DedupRecord> updatedRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to)
			throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		JdbcPagingItemReader<DedupRecord> reader = new JdbcPagingItemReader<DedupRecord>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT dedup_record_id");
		pqpf.setFromClause("FROM dedup_record_last_update");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE last_update BETWEEN :from AND :to");
		}
		pqpf.setSortKey("dedup_record_id");
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
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("solrIndexer");
		taskExecutor.setConcurrencyLimit(CONCURRENCY_LIMIT);
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
    public ItemReader<String> orphanedRecordsReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE  + "]}") Date from,
    		@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to) throws Exception {
    	if (from != null && to == null) {
    		to = new Date();
    	}
		JdbcPagingItemReader<String> reader = new JdbcPagingItemReader<String>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT dedup_record_id");
		pqpf.setFromClause("FROM dedup_record_orphaned");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE orphaned BETWEEN :from AND :to");
		}
		pqpf.setSortKey("dedup_record_id");
		reader.setRowMapper(new StringValueRowMapper());
		reader.setPageSize(20);
    	reader.setQueryProvider(pqpf.getObject());
    	reader.setDataSource(dataSource);
    	if (from != null && to != null) {
    		Map<String, Object> parameterValues = new HashMap<String, Object>();
    		parameterValues.put("from", from);
    		parameterValues.put("to", to);
    		reader.setParameterValues(parameterValues);
    	}
    	reader.afterPropertiesSet();
    	return reader;
    }

    @Bean(name="indexRecordsToSolrJob:orphanedRecordsWriter")
    @StepScope
    public OrphanedRecordsWriter orphanedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
    	return new OrphanedRecordsWriter(solrUrl);
    }

    // local records
    @Bean(name="indexLocalRecordsToSolrJob:deleteOrphanedHarvestedRecordsStep") 
    public Step deleteOrphanedHarvestedRecordsStep() throws Exception {
    	return steps.get("deleteOrphanedHarvestedRecordsStep")
                .<String, String> chunk(20) //
                .reader(orphanedHarvestedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
                .writer(orphanedRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
                .build();
    }
    
    @Bean(name="indexRecordsToSolrJob:orphanedRecordsReader")
	@StepScope
    public ItemReader<String> orphanedHarvestedRecordsReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE  + "]}") Date from,
    		@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to) throws Exception {
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
		pqpf.setSortKeys(ImmutableMap.of("import_conf_id",
				Order.ASCENDING, "record_id", Order.ASCENDING));
		reader.setRowMapper(new StringValueRowMapper());
		reader.setPageSize(PAGE_SIZE);
    	reader.setQueryProvider(pqpf.getObject());
    	reader.setDataSource(dataSource);
    	if (from != null && to != null) {
    		Map<String, Object> parameterValues = new HashMap<String, Object>();
    		parameterValues.put("from", from);
    		parameterValues.put("to", to);
    		reader.setParameterValues(parameterValues);
    	}
    	reader.afterPropertiesSet();
    	return reader;
    }

	@Bean(name="indexLocalRecordsToSolrJob:updateHarvestedRecordsStep")
	public Step updateHarvestedRecordsStep() throws Exception {
		return steps.get("updateHarvestedRecordsStep")
			.<HarvestedRecord, Future<List<SolrInputDocument>>> chunk(CHUNK_SIZE) //
			.reader(updatedHarvestedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
			.processor(asyncUpdatedHarvestedRecordsProcessor(INTEGER_OVERRIDEN_BY_EXPRESSION)) //
			.writer(updatedHarvestedRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
			.build();
	}
	
	@Bean(name = "indexLocalRecordsToSolrJob:updatedHarvestedRecordsReader")
	@StepScope
	public ItemReader<HarvestedRecord> updatedHarvestedRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to)
			throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		JdbcPagingItemReader<HarvestedRecord> reader = new JdbcPagingItemReader<HarvestedRecord>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM harvested_record");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE updated BETWEEN :from AND :to");
		}
		pqpf.setSortKeys(ImmutableMap.of("import_conf_id",
				Order.ASCENDING, "record_id", Order.ASCENDING));
		reader.setRowMapper(harvestedRecordRowMapper);
		reader.setPageSize(PAGE_SIZE);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		if (from != null && to != null) {
			Map<String, Object> parameterValues = new HashMap<String, Object>();
			parameterValues.put("from", from);
			parameterValues.put("to", to);
			reader.setParameterValues(parameterValues);
		}
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = "indexLocalRecordsToSolrJob:updatedHarvestedRecordsProcessor")
	@StepScope
	public SolrHarvestedRecordProcessor updatedHarvestedRecordsProcessor() {
		return new SolrHarvestedRecordProcessor();
	}

	@Bean(name = "indexLocalRecordsToSolrJob:asyncUpdatedHarvestedRecordsReader")
	@StepScope
	public AsyncItemProcessor<HarvestedRecord, List<SolrInputDocument>> asyncUpdatedHarvestedRecordsProcessor(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_NUMBER_OF_INDEXING_THREADS + "]}") Integer indexingThreads) {
		AsyncItemProcessor<HarvestedRecord, List<SolrInputDocument>> processor = new AsyncItemProcessor<>();
		processor.setDelegate(new DelegatingHibernateProcessor<>(sessionFactory, updatedHarvestedRecordsProcessor()));
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("solrIndexer"); 
		taskExecutor.setConcurrencyLimit((indexingThreads != null) ? indexingThreads : CONCURRENCY_LIMIT);
		processor.setTaskExecutor(taskExecutor);
		return processor;
	}

	@Bean(name="indexLocalRecordsToSolrJob:updatedHarvestedRecordsWriter")
	@StepScope
	public SolrIndexWriter updatedHarvestedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
		return new SolrIndexWriter(solrUrl);
	}

}
