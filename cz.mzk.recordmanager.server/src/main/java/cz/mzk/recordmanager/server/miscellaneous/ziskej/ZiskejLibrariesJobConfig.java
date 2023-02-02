package cz.mzk.recordmanager.server.miscellaneous.ziskej;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZiskejLibrariesJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job harvestZiskejLibrariesJob(
			@Qualifier(Constants.JOB_ID_HARVEST_ZISKEJ_LIBRARIES + ":harvestZiskejLibrariesStep") Step harvestZiskejLibrariesStep
	) {
		return jobs.get(Constants.JOB_ID_HARVEST_ZISKEJ_LIBRARIES)
				.validator(new ZiskejLibrariesJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(harvestZiskejLibrariesStep)
				.end().build();
	}


	@Bean(name = Constants.JOB_ID_HARVEST_ZISKEJ_LIBRARIES + ":harvestZiskejLibrariesStep")
	@Deprecated
	public Step harvestZiskejLibrariesStep() throws Exception {
		return steps.get("harvestZiskejLibrariesStep")
				.tasklet(harvestZiskejLibrariesTasklet(STRING_OVERRIDEN_BY_EXPRESSION))
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_ZISKEJ_LIBRARIES + ":harvestZiskejLibrariesTasklet")
	@StepScope
	public Tasklet harvestZiskejLibrariesTasklet(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat
	) {
		return new HarvestZiskejLibrariesTasklet(strFormat);
	}


}
