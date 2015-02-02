package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.springbatch.DateIntervalPartitioner;
import cz.mzk.recordmanager.server.springbatch.HibernateChunkListener;

@Configuration
public class OAIHarvestJobConfig {
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Bean
    public Job oaiHarvestJob(@Qualifier("oaiHarvestJob:step") Step step) {
        return jobs.get("oaiHarvestJob") //
        		.validator(new OAIHarvestJobParametersValidator()) //
				.flow(step) //
				.end() //
				.build();
    }
    
    @Bean(name="oaiHarvestJob:step")
    public Step step() {
        return steps.get("step1") //
            .<List<OAIRecord>, List<OAIRecord>> chunk(1) //
            .reader(reader()) //
            .writer(writer()) //
            .listener(hibernateChunkListener()) //
            .build();
    }
    
    @Bean(name="oaiHarvestJob:partitionedStep")
    public Step partitionedStep() {
    	return steps.get("step") //
    			.partitioner("slave", partioner(null, null)) //
    			.gridSize(10) //
    			.step(slaveStep()) //
    			.build();
    }
    
    @Bean(name="oaiHarvestJob:slaveStep")
    public Step slaveStep() {
        return steps.get("step1") //
            .<List<OAIRecord>, List<OAIRecord>> chunk(1) //
            .reader(reader()) //
            .writer(writer()) //
            .listener(hibernateChunkListener()) //
            .build();
    }
    
    @Bean(name="oaiHarvestJob:partioner")
    @StepScope
    public DateIntervalPartitioner partioner(@Value("#{jobParameters[from]}") Date from,
    		@Value("#{jobParameters[from]}") Date to) {
    	return new DateIntervalPartitioner(new DateTime(from), new DateTime(to),
    			Period.months(1), "to", "from");
    }
    
    @Bean(name="oaiHarvestJob:reader")
    @StepScope
    public OAIItemReader reader() {
    	return new OAIItemReader();
    }
    
    @Bean(name="oaiHarvestJob:writer")
    @StepScope
    public OAIItemWriter writer() {
    	return new OAIItemWriter();
    }
    
    @Bean
    public HibernateChunkListener hibernateChunkListener() {
    	return new HibernateChunkListener();
    }

}
