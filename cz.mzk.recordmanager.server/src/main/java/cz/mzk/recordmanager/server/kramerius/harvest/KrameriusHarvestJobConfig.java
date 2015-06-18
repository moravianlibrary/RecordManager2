package cz.mzk.recordmanager.server.kramerius.harvest;

import java.util.Date;
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

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class KrameriusHarvestJobConfig {

	
	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final Integer INTEGER_OVERRIDEN_BY_EXPRESSION = null; 
	
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Bean
    public Job krameriusHarvestJob(@Qualifier("krameriusHarvestJob:step") Step step) {
        return jobs.get("krameriusHarvestJob") //
        		.validator(new KrameriusHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
    }
    
    @Bean(name="krameriusHarvestJob:step")
    public Step step() {
        return steps.get("step") //
            .<List<HarvestedRecord>, List<HarvestedRecord>> chunk(1) //
            .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
            .writer(writer()) //
            .build();
    }
    
    @Bean(name="krameriusHarvestJob:reader")
    @StepScope
    public KrameriusItemReader reader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
    				+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE +"]}") Date from,
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE+"]"
    				+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE +"]}") Date to) {
    	return new KrameriusItemReader(configId, from, to);
    }
    
    @Bean(name="krameriusHarvestJob:writer")
    @StepScope
    public KrameriusItemWriter writer() {
    	return new KrameriusItemWriter();
    }
    
	
}
