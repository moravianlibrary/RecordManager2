package cz.mzk.recordmanager.server.miscellaneous.caslin.siglaUrl;

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
public class SiglaCaslinJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Bean
	public Job harvestSiglaCaslinJob(
			@Qualifier(Constants.JOB_ID_HARVEST_SIGLA_CASLIN + ":harvestSiglaCaslinStep") Step harvestSiglaCaslinStep
	) {
		return jobs.get(Constants.JOB_ID_HARVEST_SIGLA_CASLIN)
				.validator(new SiglaCaslinJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(harvestSiglaCaslinStep)
				.end()
				.build();
	}


	@Bean(name = Constants.JOB_ID_HARVEST_SIGLA_CASLIN + ":harvestSiglaCaslinStep")
	@Deprecated
	public Step harvestSiglaCaslinStep() {
		return steps.get("harvestSiglaCaslinStep")
				.tasklet(harvestSiglaCaslinTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_SIGLA_CASLIN + ":harvestSiglaCaslinTasklet")
	@StepScope
	public Tasklet harvestSiglaCaslinTasklet() {
		return new HarvestSiglaCaslinsTasklet();
	}

}
