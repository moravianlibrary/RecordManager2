package cz.mzk.recordmanager.server.miscellaneous.fit.fulltextAnalyser;

import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
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

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

@Configuration
public class FulltextAnalyserJobConfig {
	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;
	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job FulltextAnalyserJob(
			@Qualifier(Constants.JOB_ID_FULLTEXT_ANALYSER + ":analyserStep") Step analyserStep) {
		return jobs.get(Constants.JOB_ID_FULLTEXT_ANALYSER)
				.validator(new FulltextAnalyserJobParamatersValidator())
				.flow(analyserStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_FULLTEXT_ANALYSER + ":analyserStep")
	public Step fulltextAnalyserStep() throws Exception {
		return steps.get(Constants.JOB_ID_FULLTEXT_ANALYSER + ":analyserStep")
				.listener(new StepProgressListener())
				.<List<FulltextAnalyser>, List<FulltextAnalyser>>chunk(1)//
				.reader(FulltextAnalyserReader(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(FulltextAnalyserWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_FULLTEXT_ANALYSER + ":fulltextAnalyserReader")
	@StepScope
	public FulltextAnalyserReader FulltextAnalyserReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) throws FileNotFoundException {
		return new FulltextAnalyserReader(filename);
	}

	@Bean(name = Constants.JOB_ID_FULLTEXT_ANALYSER + ":fulltextAnalyserWriter")
	@StepScope
	public ItemWriter<List<FulltextAnalyser>> FulltextAnalyserWriter() {
		return new FulltextAnalyserWriter();
	}

}
