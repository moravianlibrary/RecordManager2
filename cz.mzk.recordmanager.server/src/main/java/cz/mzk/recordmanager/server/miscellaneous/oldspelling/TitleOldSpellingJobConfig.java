package cz.mzk.recordmanager.server.miscellaneous.oldspelling;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationImportJobParametersValidator;
import cz.mzk.recordmanager.server.model.TitleOldSpelling;
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
public class TitleOldSpellingJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job importTitleOldSpellingJob(
			@Qualifier(Constants.JOB_ID_IMPORT_TITLE_OLD_SPELLING + ":importTitleOldSpellingStep") Step importTitleOldSpellingStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_TITLE_OLD_SPELLING)
				.validator(new InspirationImportJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(importTitleOldSpellingStep)
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_TITLE_OLD_SPELLING + ":importTitleOldSpellingStep")
	public Step importTitleOldSpellingStep() {
		return steps.get("importTitleOldSpellingStep")
				.listener(new StepProgressListener())
				.<List<TitleOldSpelling>, List<TitleOldSpelling>>chunk(1)//
				.reader(titleOldSpellingFileReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(titleOldSpellingImportWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_TITLE_OLD_SPELLING + ":importTitleOldSpellingReader")
	@StepScope
	public TitleOldSpellingImportFileReader titleOldSpellingFileReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) {
		return new TitleOldSpellingImportFileReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_TITLE_OLD_SPELLING + ":writer")
	@StepScope
	public TitleOldSpellingImportWriter titleOldSpellingImportWriter() {
		return new TitleOldSpellingImportWriter();
	}


}
