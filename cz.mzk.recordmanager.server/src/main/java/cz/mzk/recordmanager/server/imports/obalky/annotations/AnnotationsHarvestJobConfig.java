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
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job importAnnotationsObalkyJob(
			@Qualifier(Constants.JOB_ID_IMPORT_ANNOTATIONS + ":importStep") Step importStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_ANNOTATIONS) //
				.validator(new AnnotationsHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(importStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANNOTATIONS + ":importStep")
	public Step importStep() throws Exception {
		return steps.get("importAnnotationsStep")
				.listener(new StepProgressListener())
				.<ObalkyKnihAnnotation, ObalkyKnihAnnotation>chunk(20)//
				.reader(importAnnotationsReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importAnnotationsWriter()) //
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

	@Bean
	public Job deleteAnnotationsObalkyJob(
			@Qualifier(Constants.JOB_ID_DELETE_ANNOTATIONS + ":deleteStep") Step deleteStep) {
		return jobs.get(Constants.JOB_ID_DELETE_ANNOTATIONS) //
				.validator(new DeleteAnnotationsJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(deleteStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_DELETE_ANNOTATIONS + ":deleteStep")
	public Step deleteStep() throws Exception {
		return steps.get("deleteAnnotationsStep")
				.listener(new StepProgressListener())
				.<ObalkyKnihAnnotation, ObalkyKnihAnnotation>chunk(20)//
				.reader(deleteAnnotationsReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION))//
				.writer(deleteAnnotationsWriter()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_DELETE_ANNOTATIONS + ":deleteReader")
	@StepScope
	public ItemReader<ObalkyKnihAnnotation> deleteAnnotationsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to
	) throws Exception {
		if (from == null) from = new Date(0);
		if (to == null) to = new Date();
		JdbcPagingItemReader<ObalkyKnihAnnotation> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM obalkyknih_annotation");
		pqpf.setWhereClause("WHERE last_harvest BETWEEN :from AND :to");
		pqpf.setSortKey("id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("from", from);
		parameterValues.put("to", to);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new BeanPropertyRowMapper<>(ObalkyKnihAnnotation.class));
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_DELETE_ANNOTATIONS + ":deleteWriter")
	@StepScope
	public DeleteAnnotationsWriter deleteAnnotationsWriter() {
		return new DeleteAnnotationsWriter();
	}

}
