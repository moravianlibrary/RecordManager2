package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.List;

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
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class OAIHarvestJobConfig {
	
	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Bean
    public Job oaiPartitionedHarvestJob(@Qualifier("oaiHarvestJob:partitionedStep") Step step) {
        return jobs.get("oaiPartitionedHarvestJob") //
        		.validator(new OAIHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
    }

    @Bean
    public Job oaiHarvestJob(@Qualifier("oaiHarvestJob:step") Step step) {
        return jobs.get("oaiHarvestJob") //
        		.validator(new OAIHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
    }
    
    @Bean(name="oaiHarvestJob:step")
    public Step step() {
        return steps.get("step1") //
            .<List<OAIRecord>, List<OAIRecord>> chunk(1) //
            .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
            .writer(writer()) //
            .build();
    }
    
    @Bean(name="oaiHarvestJob:partitionedStep")
    public Step partitionedStep() {
    	return steps.get("step") //
    			.partitioner("slave", partioner(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
    			.gridSize(10) //
    			.step(slaveStep()) //
    			.build();
    }
    
    @Bean(name="oaiHarvestJob:slaveStep")
    public Step slaveStep() {
        return steps.get("step1") //
            .<List<OAIRecord>, List<OAIRecord>> chunk(1) //
            .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
            .writer(writer()) //
            .build();
    }
    
    @Bean(name="oaiHarvestJob:partioner")
    @StepScope
    public DateIntervalPartitioner partioner(@Value("#{jobParameters[fromDate]}") Date from,
    		@Value("#{jobParameters[untilDate]}") Date to) {
    	return new DateIntervalPartitioner(from, to,
    			Period.months(1), Constants.JOB_PARAM_UNTIL_DATE, Constants.JOB_PARAM_FROM_DATE);
    }
    
    @Bean(name="oaiHarvestJob:reader")
    @StepScope
    public OAIItemReader reader(@Value("#{jobParameters[configurationId]}") Long configId, 
    		@Value("#{stepExecutionContext[fromDate]?:jobParameters[fromDate]}") Date from,
    		@Value("#{stepExecutionContext[untilDate]?:jobParameters[untilDate]}") Date to) {
    	return new OAIItemReader(configId, from, to);
    }
    
    @Bean(name="oaiHarvestJob:writer")
    @StepScope
    public OAIItemWriter writer() {
    	return new OAIItemWriter();
    }

}
