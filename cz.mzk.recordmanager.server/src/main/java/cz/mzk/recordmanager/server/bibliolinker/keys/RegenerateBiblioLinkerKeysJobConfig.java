package cz.mzk.recordmanager.server.bibliolinker.keys;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
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
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RegenerateBiblioLinkerKeysJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job RegenerateBiblioLinkerKeysJob(
			@Qualifier(Constants.JOB_ID_REGENERATE_BL_KEYS + ":regenBiblioLinkerKeysStep") Step regenBiblioLinkerKeysStep) {
		return jobs.get(Constants.JOB_ID_REGENERATE_BL_KEYS)
				.validator(new RegenerateBiblioLInkerJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.start(regenBiblioLinkerKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGENERATE_BL_KEYS + ":regenBiblioLinkerKeysStep")
	public Step regenBiblioLinkerKeysStep() throws Exception {
		return steps.get("regenBiblioLinkerKeysStep")
				.listener(new StepProgressListener())
				.<Long, Long>chunk(1)//
				.reader(regenarateBiblioLinkerKeysReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(regenarateBiblioLinkerKeysWriter()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_REGENERATE_BL_KEYS + ":regenarateBiblioLinkerKeysReader")
	@StepScope
	public ItemReader<Long> regenarateBiblioLinkerKeysReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_ID + "]}") String startRecordId
	) throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record hr");
		if (startRecordId != null) {
			pqpf.setWhereClause("WHERE id>:startId");
			Map<String, Object> parameterValues = new HashMap<>();
			parameterValues.put("startId", startRecordId);
			reader.setParameterValues(parameterValues);
		}
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_REGENERATE_BL_KEYS + ":regenarateBiblioLinkerKeysWriter")
	@StepScope
	public ItemWriter<Long> regenarateBiblioLinkerKeysWriter() throws Exception {
		return new RegenerateBiblioLinkerKeysWriter();
	}

}

