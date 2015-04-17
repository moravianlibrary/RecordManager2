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
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class IndexLocalRecordsToSolrJobConfig {

	private static Logger logger = LoggerFactory
			.getLogger(IndexLocalRecordsToSolrJobConfig.class);

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

	@Autowired
	private HarvestedRecordRowMapper harvestedRecordRowMapper;

    @Bean
    public Job indexLocalRecordsToSolrJob(@Qualifier("indexLocalRecordsToSolrJob:updateHarvestedRecordsStep") Step updateHarvestedRecordsStep) {
        return jobs.get(Constants.JOB_ID_SOLR_INDEX_LOCAL_RECORDS)
        		.validator(new IndexRecordsToSolrJobParametersValidator())
        		.listener(JobFailureListener.INSTANCE)
				.flow(updateHarvestedRecordsStep)
				.end()
				.build();
    }
    
    @Bean(name="indexLocalRecordsToSolrJob:updateHarvestedRecordsStep")
    public Step updateHarvestedRecordsStep() throws Exception {
		return steps.get("updateHarvestedRecordsStep")
            .<HarvestedRecord, SolrInputDocument> chunk(CHUNK_SIZE) //
            .reader(updatedHarvestedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
            .processor(updatedHarvestedRecordsProcessor()) //
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
		pqpf.setSortKeys(ImmutableMap.of("oai_harvest_conf_id",
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
	
    @Bean(name="indexLocalRecordsToSolrJob:updatedHarvestedRecordsWriter")
    @StepScope
    public SolrIndexWriter updatedHarvestedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
    	return new SolrIndexWriter(solrUrl);
    }

}
