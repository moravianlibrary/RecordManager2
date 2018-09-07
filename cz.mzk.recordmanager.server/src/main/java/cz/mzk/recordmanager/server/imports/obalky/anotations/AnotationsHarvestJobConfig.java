package cz.mzk.recordmanager.server.imports.obalky.anotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class AnotationsHarvestJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	// harvest metadata
	@Bean
	public Job importAnotationsObalkyJob(
			@Qualifier(Constants.JOB_ID_IMPORT_ANOTATIONS + ":importStep") Step importStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_ANOTATIONS) //
				.validator(new AnotationsHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(importStep) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANOTATIONS + ":importStep")
	public Step importStep() throws Exception {
		return steps.get("importAnotationsStep")
				.listener(new StepProgressListener())
				.<ObalkyKnihAnotation, ObalkyKnihAnotation>chunk(20)//
				.reader(importAnotationsReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importAnotationsWriter()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANOTATIONS + "reader")
	@StepScope
	public AnotationsReader importAnotationsReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) {
		return new AnotationsReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANOTATIONS + ":writer")
	@StepScope
	public AnotationsWriter importAnotationsWriter() {
		return new AnotationsWriter();
	}

}
