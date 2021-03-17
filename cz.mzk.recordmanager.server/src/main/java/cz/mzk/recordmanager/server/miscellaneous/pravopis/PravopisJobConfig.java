package cz.mzk.recordmanager.server.miscellaneous.pravopis;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationImportJobParametersValidator;
import cz.mzk.recordmanager.server.model.Pravopis;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
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

import java.util.List;

@Configuration
public class PravopisJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job importPravopisJob(
			@Qualifier(Constants.JOB_ID_IMPORT_PRAVOPIS + ":importPravopisStep") Step importPravopisStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_PRAVOPIS)
				.validator(new InspirationImportJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(importPravopisStep)
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_PRAVOPIS + ":importPravopisStep")
	public Step importPravopisStep() throws Exception {
		return steps.get("importPravopisStep")
				.listener(new StepProgressListener())
				.<List<Pravopis>, List<Pravopis>>chunk(1)//
				.reader(pravopisFileReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(pravopisImportWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_PRAVOPIS + ":importPravopisReader")
	@StepScope
	public PravopisImportFileReader pravopisFileReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
		return new PravopisImportFileReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_PRAVOPIS + ":writer")
	@StepScope
	public PravopisImportWriter pravopisImportWriter() {
		return new PravopisImportWriter();
	}


}
