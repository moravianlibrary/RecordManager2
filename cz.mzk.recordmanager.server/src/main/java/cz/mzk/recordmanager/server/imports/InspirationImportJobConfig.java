package cz.mzk.recordmanager.server.imports;

import java.util.List;
import java.util.Map;

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

import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class InspirationImportJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job inspirationImportJob(
			@Qualifier(Constants.JOB_ID_IMPORT_INSPIRATION +":importRecordsStep") Step inspirationImportStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_INSPIRATION)
				.validator(new InspirationImportJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(inspirationImportStep)
				.end().build();
	}

	@Bean(name=Constants.JOB_ID_IMPORT_INSPIRATION +":importRecordsStep")
	public Step inspirationImportStep() throws Exception {
		return steps.get("updateRecordsJobStep")
				.<Map<String, List<String>>, Map<String, List<String>>> chunk(1)//
				.reader(inspirationReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(InspirationImportWriter()) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_IMPORT_INSPIRATION +":importRecordsReader")
	@StepScope
	public InspirationImportFileReader inspirationReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
			return new InspirationImportFileReader(filename);
	}
	
	@Bean(name=Constants.JOB_ID_IMPORT_INSPIRATION +":writer")
	@StepScope
	public InspirationImportWriter InspirationImportWriter(){
		return new InspirationImportWriter();
	}

}

