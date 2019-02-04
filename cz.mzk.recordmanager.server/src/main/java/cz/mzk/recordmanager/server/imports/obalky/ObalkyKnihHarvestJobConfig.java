package cz.mzk.recordmanager.server.imports.obalky;

import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class ObalkyKnihHarvestJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job obalkyKnihHarvestJob(
			@Qualifier(Constants.JOB_ID_HARVEST_OBALKY_KNIH + ":step") Step step) {
		return jobs.get(Constants.JOB_ID_HARVEST_OBALKY_KNIH) //
				.validator(new ObalkyKnihHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_OBALKY_KNIH + ":step")
	public Step step() throws Exception {
		return steps.get(Constants.JOB_ID_HARVEST_OBALKY_KNIH + ":step")
				.<ObalkyKnihTOC, ObalkyKnihTOC> chunk(10)//
				.reader(importObalkyKnihReader(STRING_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION))//
				.writer(importObalkyKnihWriter()) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_HARVEST_OBALKY_KNIH + ":writer")
	@StepScope
	public ItemWriter<? super ObalkyKnihTOC> importObalkyKnihWriter() {
		return new ObalkyKnihRecordsWriter();
	}

	@Bean(name=Constants.JOB_ID_HARVEST_OBALKY_KNIH + ":reader")
	@StepScope
	public ItemReader<? extends ObalkyKnihTOC> importObalkyKnihReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from
	) {
		return new ObalkyKnihRecordsReader(filename, from);
	}

}
