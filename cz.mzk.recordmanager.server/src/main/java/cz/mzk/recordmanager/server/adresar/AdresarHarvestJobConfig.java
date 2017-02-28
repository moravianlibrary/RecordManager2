package cz.mzk.recordmanager.server.adresar;

import java.util.List;

import javax.sql.DataSource;

import org.marc4j.marc.Record;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class AdresarHarvestJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	private DataSource dataSource;
	
	@Bean
	public Job zakonyProLidiHarvestJob(
			@Qualifier(Constants.JOB_IF_HARVEST_ADRESAR + ":step") Step step) {
		return jobs.get(Constants.JOB_IF_HARVEST_ADRESAR) //
				.validator(new AdresarHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_IF_HARVEST_ADRESAR + ":step")
	public Step step() throws Exception {
		return steps.get(Constants.JOB_IF_HARVEST_ADRESAR + ":step")
				.<List<Record>, List<Record>> chunk(10)//
				.reader(harvestAdresarReader())//
				.writer(harvestAdresarWriter()) //
				.build();
	}

	@Bean(name=Constants.JOB_IF_HARVEST_ADRESAR + ":reader")
	@StepScope
	public ItemReader<List<Record>> harvestAdresarReader() {
		return new AdresarRecordsReader();
	}

	@Bean(name=Constants.JOB_IF_HARVEST_ADRESAR + ":writer")
	@StepScope
	public AdresarRecordsWriter harvestAdresarWriter() {
		return new AdresarRecordsWriter();
	}
}
