package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.harvest.ReharvestJobExecutionDecider;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KramAvailabilityJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job HarvestKramAvailabilityJob(
			@Qualifier(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":harvestStep") Step harvestStep,
			@Qualifier(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":afterHarvestStep") Step afterHarvestStep) {
		return jobs.get(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY)
				.validator(new KramAvailabilityJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(harvestStep)
				.next(ReharvestJobExecutionDecider.INSTANCE)
				.on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString())
				.to(afterHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE)
				.on(FlowExecutionStatus.COMPLETED.toString())
				.end() //
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":harvestStep")
	public Step harvestKramAvailabilityStep() throws Exception {
		return steps.get(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":harvestStep")
				.listener(new StepProgressListener())
				.<KramAvailability, KramAvailability>chunk(10)//
				.reader(harvestKramAvailabilityReader(LONG_OVERRIDEN_BY_EXPRESSION))//
				.writer(harvestKramAvailabilityWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":reader")
	@StepScope
	public ItemReader<KramAvailability> harvestKramAvailabilityReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId) throws Exception {
		return new KramAvailabilityReader(configId);
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":writer")
	@StepScope
	public ItemWriter<KramAvailability> harvestKramAvailabilityWriter() {
		return new KramAvailabilityWriter();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":afterHarvestStep")
	public Step afterHarvestStep() {
		return steps.get("afterHarvestStep") //
				.tasklet(afterHarvestTasklet()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY + ":afterHarvestTasklet")
	@StepScope
	public Tasklet afterHarvestTasklet() {
		return new AfterHarvestAvailabilityTasklet();
	}
}
