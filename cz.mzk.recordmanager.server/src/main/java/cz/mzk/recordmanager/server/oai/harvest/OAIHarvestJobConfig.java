package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.List;

import org.joda.time.Period;
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

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.springbatch.DateIntervalPartitioner;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class OAIHarvestJobConfig {
	
	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Value(value = "${oai_harvest.async_reader:#{false}}")
	private boolean asyncReader = false;

	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Bean
    public Job oaiPartitionedHarvestJob(@Qualifier("oaiHarvestJob:partitionedStep") Step step1, @Qualifier("oaiHarvestJob:afterHarvestStep") Step step2) {
        return jobs.get("oaiPartitionedHarvestJob") //
        		.validator(new OAIHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step1) //
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(step2) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end() //
				.build();
    }

    @Bean
    public Job oaiHarvestJob(@Qualifier("oaiHarvestJob:step") Step step1, @Qualifier("oaiHarvestJob:afterHarvestStep") Step step2) {
        return jobs.get("oaiHarvestJob") //
        		.validator(new OAIHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step1) //
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(step2) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end() //
				.build();
    }

    @Bean
    public Job oaiHarvestAuthorityJob(@Qualifier("oaiHarvestJob:authStep") Step step) {
        return jobs.get("oaiHarvestAuthorityJob") //
        		.validator(new OAIHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}

    @Bean
    public Job oaiHarvestOneByOneJob(@Qualifier("oaiHarvestJob:harvestOneByOneStep") Step step1, @Qualifier("oaiHarvestJob:afterHarvestStep") Step step2) {
        return jobs.get(Constants.JOB_ID_HARVEST_ONE_BY_ONE) //
        		.validator(new OAIHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step1) //
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(step2) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end() //
				.build();
    }

	@Bean(name="oaiHarvestJob:step")
    public Step step() {
		ItemReader<List<OAIRecord>> reader = null;
		if (this.asyncReader) {
			reader = asyncReader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION);
		} else {
			reader = reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION);
		}
        return steps.get("step1") //
            .<List<OAIRecord>, List<HarvestedRecord>> chunk(1) //
            .reader(reader) //
            .processor(oaiItemProcessor())
            .writer(harvestedRecordWriter()) //
            .build();
    }
    
    @Bean(name="oaiHarvestJob:partitionedStep")
    public Step partitionedStep() {
    	return steps.get("partitionedStep") //
    			.partitioner("slave", partioner(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
    			.gridSize(10) //
    			.step(slaveStep()) //
    			.build();
    }
    
    @Bean(name="oaiHarvestJob:slaveStep")
    public Step slaveStep() {
        return steps.get("slaveStep") //
        		 .<List<OAIRecord>, List<HarvestedRecord>> chunk(1) //
                 .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
                 .processor(oaiItemProcessor())
                 .writer(harvestedRecordWriter()) //
                 .build();
    }
    
    @Bean(name="oaiHarvestJob:harvestOneByOneStep")
    public Step harvestOneByOneStep() {
        return steps.get("harvestOneByOneStep") //
        		 .<List<OAIRecord>, List<HarvestedRecord>> chunk(1) //
                 .reader(oneByOneItemReader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
                 .processor(oaiItemProcessor())
                 .writer(harvestedRecordWriter()) //
                 .build();
    }
    
    @Bean(name="oaiHarvestJob:authStep")
    public Step authorityStep() {
        return steps.get("authStep") //
            .<List<OAIRecord>, List<OAIRecord>> chunk(1) //
            .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
            .writer(authWriter()) //
            .build();
    }
    
    @Bean(name="oaiHarvestJob:partioner")
    @StepScope
    public DateIntervalPartitioner partioner(@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
    		@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date to) {
    	return new DateIntervalPartitioner(from, to,
    			Period.months(1), Constants.JOB_PARAM_UNTIL_DATE, Constants.JOB_PARAM_FROM_DATE);
    }

	@Bean
	public Job oaiHarvestSingleRecordJob(@Qualifier("oaiHarvestSingleRecordJob:harvestSingleRecordStep") Step step) {
		return jobs.get(Constants.JOB_ID_HARVEST_SINGLE)	//
				.validator(new OAIHarvestSingleRecordJobParametersValidator()) //
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
    }

    @Bean(name="oaiHarvestSingleRecordJob:harvestSingleRecordStep")
    public Step harvestSingleStep() {
    	return steps.get("harvestSingleRecordStep") //
      			.startLimit(1) //
    			.<List<OAIRecord>, List<HarvestedRecord>> chunk(1) //
    			.reader(singleReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION) ) //
    			.processor(oaiItemProcessor()) //
    			.writer(harvestedRecordWriter()) //
    			.build();
    	
    }

	@Bean(name="oaiHarvestJob:afterHarvestStep")
	public Step afterHarvestStep() {
		return steps.get("afterHarvestStep") //
				.tasklet(afterHarvestTasklet()) //
				.build();
	}

    @Bean(name="oaiHarvestSingleRecordJob:reader")
    @StepScope
    public OAIItemSingleReader singleReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
    								  @Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_ID + "]}") String recordId) {
    	return new OAIItemSingleReader(configId, recordId);
    }

    @Bean(name="oaiHarvestJob:reader")
    @StepScope
    public OAIItemReader reader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
    				+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE +"]}") Date from,
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE+ ']'
    				+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE +"]}") Date to,
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_RESUMPTION_TOKEN+ ']'
    	    		+ "?:jobParameters[" + Constants.JOB_PARAM_RESUMPTION_TOKEN +"]}") String resumptionToken) {
    	return new OAIItemReader(configId, from, to, resumptionToken);
    }

	@Bean(name="oaiHarvestJob:asyncReader")
	@StepScope
	public AsyncOAIItemReader asyncReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
					+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE +"]}") Date from,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE+ ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE +"]}") Date to,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_RESUMPTION_TOKEN+ ']'
					+ "?:jobParameters[" + Constants.JOB_PARAM_RESUMPTION_TOKEN +"]}") String resumptionToken) {
		return new AsyncOAIItemReader(configId, from, to, resumptionToken);
	}

    @Bean(name="oaiHarvestJob:HarvestedRecordWriter")
    @StepScope
    public ItemWriter<List<HarvestedRecord>> harvestedRecordWriter() {
    	return new HarvestedRecordWriter();
    }
    
    @Bean(name="oaiHarvestJob:processor")
    @StepScope
    public OAIItemProcessor oaiItemProcessor() {
    	return new OAIItemProcessor();
    }   
    
    @Bean(name="oaiHarvestJob:authwriter")
    @StepScope
    public OAIAuthItemWriter authWriter() {
    	return new OAIAuthItemWriter();
    }
    
    @Bean(name="oaiHarvestJob:oneByOneReader")
    @StepScope    
    public OAIOneByOneItemReader oneByOneItemReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
    				+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE +"]}") Date from,
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE+ ']'
    				+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE +"]}") Date to,
    	    @Value("#{stepExecutionContext[" + Constants.JOB_PARAM_RESUMPTION_TOKEN+ ']'
    	       		+ "?:jobParameters[" + Constants.JOB_PARAM_RESUMPTION_TOKEN +"]}") String resumptionToken){
    	return new OAIOneByOneItemReader(configId, from, to, resumptionToken);
    }

	@Bean(name="oaiHarvestJob:afterHarvestTasklet")
	@StepScope
	public Tasklet afterHarvestTasklet() {
		return new AfterHarvestTasklet();
	}

}
