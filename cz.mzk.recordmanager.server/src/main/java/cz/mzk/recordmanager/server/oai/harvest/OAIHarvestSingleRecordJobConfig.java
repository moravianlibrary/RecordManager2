package cz.mzk.recordmanager.server.oai.harvest;

import java.util.List;

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
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class OAIHarvestSingleRecordJobConfig {
	
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
   
    /** 
     * Job for harvesting specific record, using predefined configuration and record id 
     */    
    @Bean 
    public Job oaiHarvestSingleRecordJob(@Qualifier("oaiHarvestSingleRecordJob:harvestSingleRecordStep") Step step) {
       return jobs.get(Constants.JOB_ID_HARVEST_SINGLE)	//
    		   .validator(new OAIHarvestSingleRecordJobParametersValidator()) //
    		   .listener(JobFailureListener.INSTANCE) //
    		   .flow(step) //
    		   .end() //
    		   .build();
    }
    
    
    
    @Bean(name="oaiHarvestSingleRecordJob:harvestSingleRecordStep")
    public Step harvestSingleStep() {
    	return steps.get("step") //
      			.startLimit(1) //
    			.<List<OAIRecord>, List<OAIRecord>> chunk(1) //
    			.reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION) ) //
    			.writer(writer()) //
    			.build();
    	
    }
    
    
    @Bean(name="oaiHarvestSingleRecordJob:reader")
    @StepScope
    public OAIItemSingleReader reader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
    								  @Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_ID + "]}") String recordId) {
    	return new OAIItemSingleReader(configId, recordId);
    }
    
    @Bean(name="oaiHarvestSingleRecordJob:writer")
    @StepScope
    public OAIItemWriter writer() {
    	return new OAIItemWriter();
    }
    

}
