package cz.mzk.recordmanager.server.miscellaneous.agrovoc;

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
public class AgrovocConvertorJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	// convert agrovoc
	@Bean
	public Job convertAgrovocRecordsJob(
			@Qualifier(Constants.JOB_ID_AGROVOC_CONVERTOR+":convertRecordsStep") Step convertRecordsStep) {
		return jobs.get(Constants.JOB_ID_AGROVOC_CONVERTOR)
				.validator(new AgrovocConvertorJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(convertRecordsStep)
				.end()
				.build();
	}

	@Bean(name=Constants.JOB_ID_AGROVOC_CONVERTOR+":convertRecordsStep")
	public Step importAgrovocRecordsStep() throws Exception {
		return steps.get(Constants.JOB_ID_AGROVOC_CONVERTOR+":convertRecordsStep")
				.<Map<String, Map<String, List<String>>>, Map<String, Map<String, List<String>>>> chunk(20)//
				.reader(AgrovocConvertorRecordsReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(agrovocConvertorWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_AGROVOC_CONVERTOR+":agrovocRecordsWriter")
	@StepScope
	public AgrovocConvertorWriter agrovocConvertorWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_OUT_FILE + "]}") String filename) {
		return new AgrovocConvertorWriter(filename);
	}

	@Bean(name=Constants.JOB_ID_AGROVOC_CONVERTOR+":agrovocRecordsReader")
	@StepScope
	public AgrovocConvertorFileReader AgrovocConvertorRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
			return new AgrovocConvertorFileReader(filename);
	}

}
