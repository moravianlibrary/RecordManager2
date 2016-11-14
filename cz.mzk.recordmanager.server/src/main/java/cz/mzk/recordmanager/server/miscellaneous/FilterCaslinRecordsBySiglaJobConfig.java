package cz.mzk.recordmanager.server.miscellaneous;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

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
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cz.mzk.recordmanager.server.export.HarvestedRecordIdRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class FilterCaslinRecordsBySiglaJobConfig {
	
	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Value(value = "${recordmanager.threadPoolSize:#{1}}")
	private int threadPoolSize = 1;
	
	@Bean
	public Job filterCaslinRecordsJob(
			@Qualifier(Constants.JOB_ID_FILTER_CASLIN+":filterCaslinRecordsStep") Step filterCaslinRecordsStep) {
		return jobs.get(Constants.JOB_ID_FILTER_CASLIN)
				.validator(new FilterCaslinRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(filterCaslinRecordsStep)
				.end()
				.build();
	}
	
	@Bean(name = Constants.JOB_ID_FILTER_CASLIN+":filterCaslinRecordsStep")
	public Step filterCaslinRecordsStep() throws Exception {
		return steps.get("updateRecordsStep")
				.<HarvestedRecordUniqueId, HarvestedRecordUniqueId> chunk(20)//
				.reader(caslinRecordsReader()) //
				.writer(caslinRecordsWriter()) //
				.taskExecutor((TaskExecutor) poolTaskExecutor()) 
				.build();
	}
	
	@Bean(name = Constants.JOB_ID_FILTER_CASLIN+":caslinRecordsReader")
	@StepScope
	public synchronized ItemReader<HarvestedRecordUniqueId> caslinRecordsReader()
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<HarvestedRecordUniqueId>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id and deleted is null");
		pqpf.setSortKey("record_id");
		Map<String, Object> parameterValues = new HashMap<String, Object>();
		parameterValues.put("conf_id", 316L);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}
	
	@Bean(name = Constants.JOB_ID_FILTER_CASLIN+":filterCaslinRecordsWriter")
	@StepScope
	public FilterCaslinRecordsWriter caslinRecordsWriter() {
		return new FilterCaslinRecordsWriter();
	}
	
	@Bean(name = Constants.JOB_ID_FILTER_CASLIN+":threadPoolTaskExecutor")
    public Executor poolTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.initialize();
        return executor;
    }
}
