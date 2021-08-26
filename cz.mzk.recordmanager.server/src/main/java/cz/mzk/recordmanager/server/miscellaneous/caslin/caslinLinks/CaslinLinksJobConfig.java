package cz.mzk.recordmanager.server.miscellaneous.caslin.caslinLinks;

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
public class CaslinLinksJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Bean
	public Job harvestCaslinLinksJob(
			@Qualifier(Constants.JOB_ID_HARVEST_CASLIN_LINKS + ":harvestCaslinLinksStep") Step harvestCaslinLinksStep
	) {
		return jobs.get(Constants.JOB_ID_HARVEST_CASLIN_LINKS)
				.validator(new CaslinLinksJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(harvestCaslinLinksStep)
				.end()
				.build();
	}


	@Bean(name = Constants.JOB_ID_HARVEST_CASLIN_LINKS + ":harvestCaslinLinksStep")
	@Deprecated
	public Step harvestCaslinLinksStep() {
		return steps.get("harvestCaslinLinksStep")
				.tasklet(harvestCaslinLinksTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_CASLIN_LINKS + ":harvestCaslinLinksTasklet")
	@StepScope
	public Tasklet harvestCaslinLinksTasklet() {
		return new HarvestCaslinLinksTasklet();
	}

}
