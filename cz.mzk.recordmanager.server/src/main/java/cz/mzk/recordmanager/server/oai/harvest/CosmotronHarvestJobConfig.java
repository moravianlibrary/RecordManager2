package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

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

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class CosmotronHarvestJobConfig {
	
	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	
	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
	private DataSource dataSource;
    
    @Bean
    public Job CosmotronHarvestJob(
    		@Qualifier(Constants.JOB_ID_HARVEST_COSMOTRON+":cosmoStep") Step cosmoStep) {
        return jobs.get(Constants.JOB_ID_HARVEST_COSMOTRON) //
        		.validator(new OAIHarvestJobParametersValidator()) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(cosmoStep) //
				.end()
				.build();
    }
    
    @Bean(name=Constants.JOB_ID_HARVEST_COSMOTRON+":cosmoStep")
    public Step cosmoStep() {
        return steps.get("step1") //
            .<List<OAIRecord>, List<HarvestedRecord>> chunk(1) //
            .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
            .processor(cosmotronItemProcessor())
            .writer(harvestedRecordWriter()) //
            .build();
    }  

    @Bean(name=Constants.JOB_ID_HARVEST_COSMOTRON+":reader")
    @StepScope
    public OAIItemReader reader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE + "] "
    				+ "?:jobParameters[ " + Constants.JOB_PARAM_FROM_DATE +"]}") Date from,
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE+"]"
    				+ "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE +"]}") Date to,
    		@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_RESUMPTION_TOKEN+"]"
    	    		+ "?:jobParameters[" + Constants.JOB_PARAM_RESUMPTION_TOKEN +"]}") String resumptionToken) {
    	return new OAIItemReader(configId, from, to, resumptionToken);
    }
    
    @Bean(name=Constants.JOB_ID_HARVEST_COSMOTRON+":HarvestedRecordWriter")
    @StepScope
    public ItemWriter<List<HarvestedRecord>> harvestedRecordWriter() {
    	return new HarvestedRecordWriter();
    }
    
    @Bean(name=Constants.JOB_ID_HARVEST_COSMOTRON+":processor")
    @StepScope
    public CosmotronItemProcessor cosmotronItemProcessor() {
    	return new CosmotronItemProcessor();
    }  
}
