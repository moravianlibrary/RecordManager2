package cz.mzk.recordmanager.server.dedup;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Configuration
public class DedupKeysGeneratorJobConfig {
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Bean
    public Job dedupKeysGeneratorJob(JobBuilderFactory jobs, Step step) {
        return jobs.get("dedupKeysGeneratorJob")
				.flow(step)
				.end()
				.build();
    }
    
    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, DedupKeysGeneratorReader reader,
    		DedupKeysGeneratorWriter writer) {
        return steps.get("step")
            .<HarvestedRecord, HarvestedRecord> chunk(1)
            .reader(reader)
            .writer(writer)
            .build();
    }
	
	@Bean
    public DedupKeysGeneratorReader reader() {
    	return new DedupKeysGeneratorReader();
    }
    
    @Bean
    public DedupKeysGeneratorWriter writer() {
    	return new DedupKeysGeneratorWriter();
    }

}
