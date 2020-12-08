package cz.mzk.recordmanager.server.miscellaneous.fit.classifier;

import cz.mzk.recordmanager.server.imports.ImportRecordsFileReader;
import cz.mzk.recordmanager.server.imports.ImportRecordsJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.marc4j.marc.Record;
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
import org.springframework.core.task.TaskExecutor;

import java.util.List;

@Configuration
public class ClassifierJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	// semantic enrichment
	@Bean
	public Job ClassifierJob(
			@Qualifier(Constants.JOB_ID_CLASSIFIER + ":classifierStep") Step classifierStep) {
		return jobs.get(Constants.JOB_ID_CLASSIFIER)
				.validator(new ImportRecordsJobParametersValidator())
				.flow(classifierStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_CLASSIFIER + ":classifierStep")
	public Step importClassifierStep() throws Exception {
		return steps.get(Constants.JOB_ID_CLASSIFIER + ":classifierStep")
				.listener(new StepProgressListener())
				.<List<Record>, List<Record>>chunk(10)//
				.reader(importClassifierReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(importClassifierWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_CLASSIFIER + ":classifierReader")
	@StepScope
	public ImportRecordsFileReader importClassifierReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
		return new ImportRecordsFileReader(configurationId, filename, strFormat);
	}

	@Bean(name = Constants.JOB_ID_CLASSIFIER + ":classifierWriter")
	@StepScope
	public ItemWriter<List<Record>> importClassifierWriter(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId
	) {
		return new ImportClassifierWriter(configurationId);
	}

}
