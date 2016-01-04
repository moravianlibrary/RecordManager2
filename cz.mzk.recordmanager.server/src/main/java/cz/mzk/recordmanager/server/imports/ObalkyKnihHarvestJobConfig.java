package cz.mzk.recordmanager.server.imports;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class ObalkyKnihHarvestJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

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
				.reader(importObalkyKnihReader())//
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
	public ItemReader<? extends ObalkyKnihTOC> importObalkyKnihReader() {
		return new ObalkyKnihRecordsReader();
	}

}
