package cz.mzk.recordmanager.server.miscellaneous.fit.semanticEnrichment;

import cz.mzk.recordmanager.server.miscellaneous.fit.fulltextAnalyser.FulltextAnalyserJobParamatersValidator;
import cz.mzk.recordmanager.server.model.FitKnowledgeBase;
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

import java.io.FileNotFoundException;
import java.util.List;

@Configuration
public class SemanticEnrichmentJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	// import knowledge base
	@Bean
	public Job ImportKnowledgeBaseJob(
			@Qualifier(Constants.JOB_ID_IMPORT_KNOWLEDGE_BASE + ":importStep") Step importStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_KNOWLEDGE_BASE)
				.validator(new FulltextAnalyserJobParamatersValidator())
				.flow(importStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_KNOWLEDGE_BASE + ":importStep")
	public Step importKnowledgeBaseStep() throws Exception {
		return steps.get(Constants.JOB_ID_IMPORT_KNOWLEDGE_BASE + ":importStep")
				.listener(new StepProgressListener())
				.<List<FitKnowledgeBase>, List<FitKnowledgeBase>>chunk(1)//
				.reader(KnowledgeBaseReader(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(KnowledgeBaseWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_KNOWLEDGE_BASE + ":importReader")
	@StepScope
	public ImportKnowledgeBaseReader KnowledgeBaseReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) throws FileNotFoundException {
		return new ImportKnowledgeBaseReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_KNOWLEDGE_BASE + ":importWriter")
	@StepScope
	public ItemWriter<List<FitKnowledgeBase>> KnowledgeBaseWriter() {
		return new ImportKnowledgeBaseWriter();
	}

	// semantic enrichment
	@Bean
	public Job SemanticEnrichmentJob(
			@Qualifier(Constants.JOB_ID_SEMANTIC_ENRICHMENT + ":enrichmentStep") Step enrichmentStep) {
		return jobs.get(Constants.JOB_ID_SEMANTIC_ENRICHMENT)
				.validator(new FulltextAnalyserJobParamatersValidator())
				.flow(enrichmentStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_SEMANTIC_ENRICHMENT + ":enrichmentStep")
	public Step semanticEnrichmentStep() throws Exception {
		return steps.get(Constants.JOB_ID_SEMANTIC_ENRICHMENT + ":enrichmentStep")
				.listener(new StepProgressListener())
				.<List<SemanticEnrichment>, List<SemanticEnrichment>>chunk(1)//
				.reader(SemanticEnrichmentReader(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(SemanticEnrichmentWriter()) //
				.taskExecutor(this.taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_SEMANTIC_ENRICHMENT + ":enrichmentReader")
	@StepScope
	public SemanticEnrichmentReader SemanticEnrichmentReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) throws FileNotFoundException {
		return new SemanticEnrichmentReader(filename);
	}

	@Bean(name = Constants.JOB_ID_SEMANTIC_ENRICHMENT + ":enrichmentWriter")
	@StepScope
	public ItemWriter<List<SemanticEnrichment>> SemanticEnrichmentWriter() {
		return new SemanticEnrichmentWriter();
	}

}
