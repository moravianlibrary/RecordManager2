package cz.mzk.recordmanager.server.index;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.jdbc.DedupRecordRowMapper;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class IndexRecordsToSolrJobConfig {
	
	private static Logger logger = LoggerFactory.getLogger(IndexRecordsToSolrJobConfig.class);
	
	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final int CHUNK_SIZE = 1000;

	private static final int PAGE_SIZE = 5000;

	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
    private DataSource dataSource;
    
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
    
    @Bean(name="indexRecordsToSolrJob:updateRecordsStep")
    public Step updateRecordsStep() throws Exception {
		return steps.get("updateRecordsJobStep")
            .<DedupRecord, SolrInputDocument> chunk(CHUNK_SIZE) //
            .reader(updatedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
            .processor(updatedRecordsProcessor()) //
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
	
    @Bean(name="indexRecordsToSolrJob:updatedRecordsReader")
	@StepScope
    public ItemReader<DedupRecord> updatedRecordsReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE  + "]}") Date from,
    		@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to) throws Exception {
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
		reader.setRowMapper(new DedupRecordRowMapper());
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
    
    @Bean(name="indexRecordsToSolrJob:updatedRecordsWriter")
    @StepScope
    public SolrIndexWriter updatedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
    	return new SolrIndexWriter(solrUrl);
    }
    
    @Bean(name="indexRecordsToSolrJob:orphanedRecordsReader")
	@StepScope
    public ItemReader<Long> orphanedRecordsReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE  + "]}") Date from,
    		@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to) throws Exception {
    	if (from != null && to == null) {
    		to = new Date();
    	}
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<Long>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT dedup_record_id");
		pqpf.setFromClause("FROM dedup_record_orphaned");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE orphaned BETWEEN :from AND :to");
		}
		pqpf.setSortKey("dedup_record_id");
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
    	reader.afterPropertiesSet();
    	return reader;
    }
    
    @Bean(name="indexRecordsToSolrJob:orphanedRecordsWriter")
    @StepScope
    public OrphanedRecordsWriter orphanedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
    	return new OrphanedRecordsWriter(solrUrl);
    }

}
