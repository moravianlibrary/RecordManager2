package cz.mzk.recordmanager.server.miscellaneous.caslin.view;

import cz.mzk.recordmanager.server.export.HarvestedRecordIdRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
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

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CaslinViewJobsConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	// updateCaslinRecordsViewJob
	@Bean
	public Job updateCaslinRecordsViewJob(
			@Qualifier(Constants.JOB_ID_UPDATE_CASLIN_VIEW + ":updateCaslinRecordsViewStep") Step updateCaslinRecordsViewStep) {
		return jobs.get(Constants.JOB_ID_UPDATE_CASLIN_VIEW)
				.validator(new UpdateCaslinRecordsViewValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(updateCaslinRecordsViewStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_UPDATE_CASLIN_VIEW + ":updateCaslinRecordsViewStep")
	public Step updateCaslinRecordsViewStep() throws Exception {
		return steps.get("updateCaslinRecordsViewStep")
				.listener(new StepProgressListener())
				.<HarvestedRecordUniqueId, HarvestedRecordUniqueId>chunk(1)//
				.reader(updateCaslinRecordsViewReader()) //
				.writer(updateCaslinRecordsViewWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_UPDATE_CASLIN_VIEW + ":updateCaslinRecordsViewReader")
	@StepScope
	public synchronized ItemReader<HarvestedRecordUniqueId> updateCaslinRecordsViewReader()
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id=:conf_id AND deleted is null");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", Constants.IMPORT_CONF_ID_CASLIN);
		reader.setParameterValues(parameterValues);
		pqpf.setSortKey("record_id");

		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(200);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_UPDATE_CASLIN_VIEW + ":updateCaslinRecordsViewWriter")
	@StepScope
	public UpdateCaslinRecordsViewWriter updateCaslinRecordsViewWriter(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_VIEW + "]}") String view
	) {
		return new UpdateCaslinRecordsViewWriter(view);
	}

}
