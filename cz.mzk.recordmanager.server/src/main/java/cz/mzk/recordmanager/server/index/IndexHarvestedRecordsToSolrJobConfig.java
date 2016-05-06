package cz.mzk.recordmanager.server.index;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.apache.solr.common.SolrInputDocument;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
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

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.DelegatingHibernateProcessor;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class IndexHarvestedRecordsToSolrJobConfig {

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final int CHUNK_SIZE = 1000;

	private static final int PAGE_SIZE = 20000;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TaskExecutor taskExecutor;

	@Bean
	public Job indexHarvestedRecordsToSolrJob(
			@Qualifier("indexHarvestedRecordsToSolrJob:updateRecordsStep") Step updateRecordsStep,
			@Qualifier("indexHarvestedRecordsToSolrJob:deleteHarvestedRecordsStep") Step deleteHarvestedRecordsStep) {
		return jobs.get(Constants.JOB_ID_SOLR_INDEX_HARVESTED_RECORDS)
				.validator(new IndexRecordsToSolrJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(updateRecordsStep)
				.next(deleteHarvestedRecordsStep)
				.end().build();
	}

	@Bean(name = "indexHarvestedRecordsToSolrJob:updateRecordsStep")
	public Step updateRecordsStep() throws Exception {
		return steps
				.get("updateRecordsJobStep")
				.<HarvestedRecord, Future<List<SolrInputDocument>>> chunk(CHUNK_SIZE)
				.reader(updatedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
				.processor(asyncUpdatedRecordsProcessor()) //
				.writer(updatedRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = "indexHarvestedRecordsToSolrJob:updatedRecordsReader")
	@StepScope
	public JdbcPagingItemReader<HarvestedRecord> updatedRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to)
			throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id, import_conf_id, record_id, updated, deleted, raw_record, format");
		pqpf.setFromClause("FROM harvested_record");
		String whereClause = "WHERE deleted IS NULL";
		if (from != null && to != null) {
			whereClause += " AND updated BETWEEN :from AND :to";
		}
		pqpf.setWhereClause(whereClause);
		if (from != null && to != null) {
			pqpf.setSortKeys(ImmutableMap.of("updated", Order.ASCENDING, "id", Order.ASCENDING));
		} else {
			pqpf.setSortKeys(ImmutableMap.of("id", Order.ASCENDING));
		}
		JdbcPagingItemReader<HarvestedRecord> reader = new JdbcPagingItemReader<>();
		reader.setRowMapper(harvestedRecordRowMapper());
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

	@Bean(name = "indexHarvestedRecordsToSolrJob:asyncUpdatedRecordsProcessor")
	@StepScope
	public AsyncItemProcessor<HarvestedRecord, List<SolrInputDocument>> asyncUpdatedRecordsProcessor() {
		AsyncItemProcessor<HarvestedRecord, List<SolrInputDocument>> processor = new AsyncItemProcessor<>();
		processor.setDelegate(new DelegatingHibernateProcessor<>(
				sessionFactory, updatedRecordsProcessor()));
		processor.setTaskExecutor(taskExecutor);
		return processor;
	}

	@Bean(name = "indexHarvestedRecordsToSolrJob:updatedRecordsWriter")
	@StepScope
	public SolrIndexWriter updatedRecordsWriter(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
		return new SolrIndexWriter(solrUrl);
	}

	@Bean(name = "indexHarvestedRecordsToSolrJob:updatedRecordsProcessor")
	@StepScope
	public SolrHarvestedRecordProcessor updatedRecordsProcessor() {
		return new SolrHarvestedRecordProcessor();
	}

	@Bean(name = "indexHarvestedRecordsToSolrJob:harvestedRecordRowMapper")
	public HarvestedRecordRowMapper harvestedRecordRowMapper() {
		return new HarvestedRecordRowMapper();
	}

	@Bean(name = "indexHarvestedRecordsToSolrJob:deleteHarvestedRecordsStep")
	public Step deleteOrphanedRecordsStep() throws Exception {
		return steps.get("deleteHarvestedRecordsStep")
				.<HarvestedRecord, HarvestedRecord> chunk(20) //
				.reader(deletedHarvestedRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
				.writer(deletedHarvestedRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name="indexHarvestedRecordsToSolrJob:orphanedRecordsWriter")
	@StepScope
	public DeletedHarvestedRecordsWriter deletedHarvestedRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
		return new DeletedHarvestedRecordsWriter(solrUrl);
	}

	@Bean(name = "indexHarvestedRecordsToSolrJob:deletedHarvestedRecordsReader")
	@StepScope
	public JdbcPagingItemReader<HarvestedRecord> deletedHarvestedRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to)
			throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		JdbcPagingItemReader<HarvestedRecord> reader = new JdbcPagingItemReader<HarvestedRecord>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id, import_conf_id, record_id, updated, deleted, raw_record, format");
		pqpf.setFromClause("FROM harvested_record");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE deleted BETWEEN :from AND :to");
		} else {
			pqpf.setWhereClause("WHERE deleted IS NOT NULL");
		}
		pqpf.setSortKeys(ImmutableMap.of("import_conf_id", Order.ASCENDING,
				"record_id", Order.ASCENDING));
		reader.setRowMapper(harvestedRecordRowMapper());
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
