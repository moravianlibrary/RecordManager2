package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.springbatch.IntrospectiveJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;

@Configuration
public class DeleteAllHarvestsJobConfig {
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    private List<String> deleteSqlScripts = Arrays.asList(
    		"DELETE FROM dedup_record", //
    		"DELETE FROM harvested_record" //
    );
    
    private enum DeleteAllHarvestsJobParameterValidator implements IntrospectiveJobParametersValidator {
    	
    	INSTANCE;

		@Override
		public void validate(JobParameters parameters)
				throws JobParametersInvalidException {
		}

		@Override
		public Collection<JobParameterDeclaration> getParameters() {
			return Collections.emptyList();
		}

    }
    
    @Bean
    public Job deleteAllHarvestsJob(@Qualifier("deleteAllHarvestsJob:step") Step step) {
        return jobs.get("deleteAllHarvestsJob") //
        		.validator(DeleteAllHarvestsJobParameterValidator.INSTANCE) //
        		.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
    }
    
    @Bean(name="deleteAllHarvestsJob:step")
    public Step step() {
        return steps.get("step") //
        	.tasklet(deleteTasklet())//
            .build();
    }

    @Bean(name="deleteAllHarvestsJob:deleteTasklet")
    @StepScope
    public Tasklet deleteTasklet() {
    	return new SqlCommandTasklet(deleteSqlScripts);
    }
    
}
