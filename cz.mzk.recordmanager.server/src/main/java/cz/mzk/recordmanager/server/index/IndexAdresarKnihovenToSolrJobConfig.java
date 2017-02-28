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
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import cz.mzk.recordmanager.server.model.AdresarKnihoven;
import cz.mzk.recordmanager.server.springbatch.DelegatingHibernateProcessor;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class IndexAdresarKnihovenToSolrJobConfig {

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final int CHUNK_SIZE = 250;

	private static final int PAGE_SIZE = 5000;

	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TaskExecutor taskExecutor;

	@Bean
	public Job indexAdresarKnihovenToSolrJob(
			@Qualifier(Constants.JOB_ID_SOLR_INDEX_ADRESAR_KNIHOVEN + ":indexRecordsStep") Step indexRecordsStep) {
		return jobs.get(Constants.JOB_ID_SOLR_INDEX_ADRESAR_KNIHOVEN)
				.validator(new IndexRecordsToSolrJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(indexRecordsStep)
				.end()
				.build();
	}

	@Bean(name=Constants.JOB_ID_SOLR_INDEX_ADRESAR_KNIHOVEN + ":indexRecordsStep")
	public Step indexRecordsStep() throws Exception {
		return steps.get("indexRecordsStep")
			.<AdresarKnihoven, Future<List<SolrInputDocument>>> chunk(CHUNK_SIZE) //
			.reader(indexRecordsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
			.processor(asyncIndexRecordsProcessor()) //
			.writer(IndexRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
			.build();
	}

	@Bean(name = Constants.JOB_ID_SOLR_INDEX_ADRESAR_KNIHOVEN + ":indexRecordsReader")
	@StepScope
	public JdbcPagingItemReader<AdresarKnihoven> indexRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to)
			throws Exception {
		if (from != null && to == null) {
			to = new Date();
		}
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM adresar_knihoven");
		if (from != null && to != null) {
			pqpf.setWhereClause("WHERE updated BETWEEN :from AND :to");
		}
		pqpf.setSortKey("id");
		JdbcPagingItemReader<AdresarKnihoven> reader = new JdbcPagingItemReader<AdresarKnihoven>();
		reader.setRowMapper(new AdresarKnihovenRowMapper());
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

    @Bean(name=Constants.JOB_ID_SOLR_INDEX_ADRESAR_KNIHOVEN + ":indexRecordsProcessor")
	@StepScope
	public SolrAdresarKnihovenProcessor indexRecordsProcessor() {
		return new SolrAdresarKnihovenProcessor();
	}

	@Bean(name = Constants.JOB_ID_SOLR_INDEX_ADRESAR_KNIHOVEN + ":asyncIndexRecordsProcessor")
	@StepScope
	public AsyncItemProcessor<AdresarKnihoven, List<SolrInputDocument>> asyncIndexRecordsProcessor() {
		AsyncItemProcessor<AdresarKnihoven, List<SolrInputDocument>> processor = new AsyncItemProcessor<>();
		processor.setDelegate(new DelegatingHibernateProcessor<>(sessionFactory, indexRecordsProcessor()));
		processor.setTaskExecutor(taskExecutor);
		return processor;
	}

    @Bean(name=Constants.JOB_ID_SOLR_INDEX_ADRESAR_KNIHOVEN + ":indexRecordsWriter")
    @StepScope
    public SolrIndexWriter IndexRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_SOLR_URL + "]}") String solrUrl) {
    	return new SolrIndexWriter(solrUrl);
    }

}
