package cz.mzk.recordmanager.server.imports.classifier;

import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;
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

@Configuration
public class ClassifierImportJobConfig {

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Bean
	public Job importClassifierRecordsJob(
			@Qualifier(Constants.JOB_ID_IMPORT_CLASSIFIER + ":importClassifierStep") Step classifierImportStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_CLASSIFIER)
				.validator(new ClassifierImportJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(classifierImportStep)
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_CLASSIFIER + ":importClassifierStep")
	public Step inspirationImportStep() throws Exception {
		return steps.get("updateRecordsStep")
				.<PredictedRecord, PredictedRecord>chunk(1)//
				.reader(classifierFileReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(classifierImportWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_CLASSIFIER + ":importClassifierReader")
	@StepScope
	public ClassifierImportFileReader classifierFileReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
		return new ClassifierImportFileReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_CLASSIFIER + ":writer")
	@StepScope
	public ClassifierImportWriter classifierImportWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId) {
		return new ClassifierImportWriter(configurationId);
	}

}

