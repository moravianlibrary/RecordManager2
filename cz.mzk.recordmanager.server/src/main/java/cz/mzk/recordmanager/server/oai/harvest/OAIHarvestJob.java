package cz.mzk.recordmanager.server.oai.harvest;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.oai.model.OAIRecord;

@Configuration
public class OAIHarvestJob {
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Bean
    public Job job() {
        return jobs.get("oaiHarvestJob").start(step1()).build();
    }
    
    @Bean
    protected Step step1() {
    	OAIItemReader reader = new OAIItemReader();
    	OAIItemWriter writer = new OAIItemWriter();
        return steps.get("step1")
            .<List<OAIRecord>, List<OAIRecord>> chunk(1)
            .reader(reader)
            .writer(writer)
            .build();
    }

}
