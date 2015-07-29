package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class KrameriusFulltextJobConfig {

	public static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;


	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Bean
	public Job krameriusFulltextJob(
			@Qualifier("krameriusFulltextJob:step") Step step) {
		return jobs.get("krameriusFulltextJob") //
				.validator(new KrameriusFulltextJobParametersValidator()) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}
	
	@Bean(name = "krameriusFulltextJob:step")
	public Step step() {
		return steps
				.get("step")
				//
				.<List<String>, List<String>> chunk(1)
				//
				.reader(reader(LONG_OVERRIDEN_BY_EXPRESSION))
				//
//				.processor(krameriusItemProcessor())
				.writer(krameriusFulltextWriter())
				.build();
	}
	
	
	@Bean(name = "krameriusFulltextJob:reader")
	@StepScope
	public KrameriusFulltextReader reader(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId) {
		return new KrameriusFulltextReader(configId);
	}
		
	@Bean(name = "krameriusFulltextJob:writer")
	@StepScope
	public ItemWriter<List<String>> krameriusFulltextWriter() {
		return new KrameriusFulltextWriter();
	}
}
