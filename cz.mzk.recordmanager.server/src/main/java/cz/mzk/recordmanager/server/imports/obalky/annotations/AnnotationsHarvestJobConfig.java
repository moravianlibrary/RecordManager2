package cz.mzk.recordmanager.server.imports.obalky.annotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AnnotationsHarvestJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ApplicationContext appCtx;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job importAnnotationsObalkyJob(
			@Qualifier(Constants.JOB_ID_IMPORT_ANNOTATIONS + ":importStep") Step importStep,
			@Qualifier(Constants.JOB_ID_IMPORT_ANNOTATIONS + ":deleteStep") Step deleteStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_ANNOTATIONS) //
				.validator(new AnnotationsHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.start(importStep) //
				.next(deleteStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANNOTATIONS + ":importStep")
	public Step importStep() throws Exception {
		return steps.get("importAnnotationsStep")
				.listener(new StepProgressListener())
				.<ObalkyKnihAnnotation, ObalkyKnihAnnotation>chunk(20)//
				.reader(importAnnotationsReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importAnnotationsWriter()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANNOTATIONS + "reader")
	@StepScope
	public AnnotationsReader importAnnotationsReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) {
		return new AnnotationsReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANNOTATIONS + ":writer")
	@StepScope
	public AnnotationsWriter importAnnotationsWriter() {
		return new AnnotationsWriter();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANNOTATIONS + ":deleteStep")
	public Step deleteStep() throws Exception {
		return steps.get("deleteAnnotationsStep")
				.listener(new StepProgressListener())
				.<ObalkyKnihAnnotation, ObalkyKnihAnnotation>chunk(20)//
				.reader(deleteAnnotationsReader())//
				.writer(deleteAnnotationsWriter()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANNOTATIONS + ":deleteReader")
	@StepScope
	public ItemReader<ObalkyKnihAnnotation> deleteAnnotationsReader() throws Exception {
		JdbcPagingItemReader<ObalkyKnihAnnotation> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM obalkyknih_annotation");
		pqpf.setWhereClause("WHERE last_harvest < :start_time");
		pqpf.setSortKey("id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("start_time", new Date(appCtx.getStartupDate()));
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new BeanPropertyRowMapper<>(ObalkyKnihAnnotation.class));
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANNOTATIONS + ":deleteWriter")
	@StepScope
	public deleteAnnotationsWriter deleteAnnotationsWriter() {
		return new deleteAnnotationsWriter();
	}

}
