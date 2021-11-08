package cz.mzk.recordmanager.server.miscellaneous.topResults;

import cz.mzk.recordmanager.server.miscellaneous.EmptyJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopResultsJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Bean
	public Job generateTopResultsJob(
			@Qualifier(Constants.JOB_ID_GENERATE_TOP_RESULTS + ":generateTopResultsStep") Step generateTopResultsStep) {
		return jobs.get(Constants.JOB_ID_GENERATE_TOP_RESULTS)
				.validator(new EmptyJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.start(generateTopResultsStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_TOP_RESULTS + ":generateTopResultsStep")
	public Step generateTopResultsStep() {
		return steps.get("generateTopResultsStep")
				.tasklet(topResultsTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_TOP_RESULTS + ":topResultsTasklet")
	@StepScope
	public Tasklet topResultsTasklet() {
		return new TopResultsTasklet();
	}

}
