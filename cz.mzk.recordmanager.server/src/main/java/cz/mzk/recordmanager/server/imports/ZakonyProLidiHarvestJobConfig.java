package cz.mzk.recordmanager.server.imports;

import java.util.List;

import org.marc4j.marc.Record;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class ZakonyProLidiHarvestJobConfig {
	
	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	
	@Bean
	public Job zakonyProLidiHarvestJob(
			@Qualifier(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":step") Step step) {
		return jobs.get(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI) //
				.validator(new ZakonyProLidiHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":step")
	public Step step() throws Exception {
		return steps.get(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":step")
				.<List<Record>, List<Record>> chunk(10)//
				.reader(importZakonyProLidiReader())//
				.writer(importZakonyProLidiWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":reader")
	@StepScope
	public ItemReader<List<Record>> importZakonyProLidiReader() {
		return new ZakonyProLidiRecordsReader();
	}

	@Bean(name=Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":writer")
	@StepScope
	public ImportRecordsWriter importZakonyProLidiWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId) {
		return new ImportRecordsWriter(configurationId);
	}

}
