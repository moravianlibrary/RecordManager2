package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.imports.antikvariaty.AfterImportAntikvariatyTasklet;
import cz.mzk.recordmanager.server.imports.antikvariaty.AntikvariatyImportJobParametersValidator;
import cz.mzk.recordmanager.server.imports.antikvariaty.AntikvariatyRecordsReader;
import cz.mzk.recordmanager.server.imports.antikvariaty.AntikvariatyRecordsWriter;
import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.harvest.AfterHarvestTasklet;
import cz.mzk.recordmanager.server.oai.harvest.HarvestedRecordWriter;
import cz.mzk.recordmanager.server.oai.harvest.OAIItemProcessor;
import cz.mzk.recordmanager.server.oai.harvest.ReharvestJobExecutionDecider;
import cz.mzk.recordmanager.server.oai.harvest.cosmotron.CosmotronRecordWriter;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;
import org.marc4j.marc.Record;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
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
public class ImportRecordJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	@Value(value = "${oai_harvest.async_reader:#{false}}")
	private boolean asyncReader = false;

	@Bean
	public Job ImportRecordsJob(
			@Qualifier(Constants.JOB_ID_IMPORT + ":importRecordsStep") Step importRecordsStep,
			@Qualifier(Constants.JOB_ID_IMPORT + ":afterHarvestStep") Step afterHarvestStep) {
		return jobs.get(Constants.JOB_ID_IMPORT)
				.validator(new ImportRecordsJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(importRecordsStep)
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(afterHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT + ":importRecordsStep")
	public Step importRecordsStep() throws Exception {
		return steps.get("importRecordsStep")
				.listener(new StepProgressListener())
				.<List<Record>, List<Record>>chunk(20)//
				.reader(importRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importRecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT + ":importRecordsReader")
	@StepScope
	public synchronized ItemReader<List<Record>> importRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
		return new ImportRecordsFileReader(configurationId, filename, strFormat);
	}

	@Bean(name = Constants.JOB_ID_IMPORT + ":writer")
	@StepScope
	public ImportRecordsWriter importRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId) {
		return new ImportRecordsWriter(configurationId);
	}

	// multi thread import
	@Bean
	public Job multiImportRecordsJob(
			@Qualifier(Constants.JOB_ID_MULTI_THREADS_IMPORT + ":importRecordsStep") Step multiImportRecordsStep,
			@Qualifier(Constants.JOB_ID_IMPORT + ":afterHarvestStep") Step afterHarvestStep) {
		return jobs.get(Constants.JOB_ID_MULTI_THREADS_IMPORT)
				.validator(new ImportRecordsJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(multiImportRecordsStep)
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(afterHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_MULTI_THREADS_IMPORT + ":importRecordsStep")
	public Step multiImportRecordsStep() throws Exception {
		return steps.get("multiImportRecordsStep")
				.listener(new StepProgressListener())
				.<List<Record>, List<Record>>chunk(20)//
				.reader(importRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importRecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.taskExecutor(taskExecutor)
				.build();
	}

	// Download and import
	@Bean
	public Job DownloadAndImportRecordsJob(
			@Qualifier(Constants.JOB_ID_DOWNLOAD_IMPORT + ":downloadImportRecordsStep") Step downloadImportRecordsStep,
			@Qualifier(Constants.JOB_ID_IMPORT + ":afterHarvestStep") Step afterHarvestStep) {
		return jobs.get(Constants.JOB_ID_DOWNLOAD_IMPORT)
				.validator(new DownloadAndImportRecordsJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(downloadImportRecordsStep)
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(afterHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_DOWNLOAD_IMPORT + ":downloadImportRecordsStep")
	public Step downloadImportRecordsStep() throws Exception {
		return steps.get("downloadImportRecordsStep")
				.listener(new StepProgressListener())
				.<List<Record>, List<Record>>chunk(20)//
				.reader(downloadImportRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION))//
				.writer(importRecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_DOWNLOAD_IMPORT + ":importRecordsReader")
	@StepScope
	public ItemReader<List<Record>> downloadImportRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long confId)
			throws Exception {
		return new ImportRecordsFileReader(confId);
	}

	@Bean(name = Constants.JOB_ID_IMPORT + ":afterHarvestStep")
	public Step afterHarvestStep() {
		return steps.get("afterHarvestStep") //
				.listener(new StepProgressListener())
				.tasklet(afterHarvestTasklet()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT + ":afterHarvestTasklet")
	@StepScope
	public Tasklet afterHarvestTasklet() {
		return new AfterHarvestTasklet();
	}

	// Antikvariaty
	@Bean
	public Job AntikvariatyImportRecordsJob(
			@Qualifier(Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":importRecordsStep") Step importRecordsStep,
			@Qualifier(Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":udpateRecordsStep") Step updateRecordsStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_ANTIKVARIATY)
				.validator(new AntikvariatyImportJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(importRecordsStep)
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(updateRecordsStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end()
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":importRecordsStep")
	public Step importAntikvariatyRecordsStep() throws Exception {
		return steps.get("antikvariaty:importRecordsStep")
				.listener(new StepProgressListener())
				.<AntikvariatyRecord, AntikvariatyRecord>chunk(10)//
				.reader(importAntikvariatyReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importAntikvariatyWriter()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":reader")
	@StepScope
	public ItemReader<AntikvariatyRecord> importAntikvariatyReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) throws Exception {
		return new AntikvariatyRecordsReader(configId, filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":writer")
	@StepScope
	public ItemWriter<AntikvariatyRecord> importAntikvariatyWriter() {
		return new AntikvariatyRecordsWriter();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":udpateRecordsStep")
	public Step updateRecordsStep() {
		return steps.get(Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":udpateRecordsStep") //
				.listener(new StepProgressListener())
				.tasklet(afterImportAntikvariatyTasklet()) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_ANTIKVARIATY + ":afterImportAntikvariatyTasklet")
	@StepScope
	public Tasklet afterImportAntikvariatyTasklet() {
		return new AfterImportAntikvariatyTasklet();
	}

	// Oai format
	@Bean
	public Job OaiImportRecordsJob(
			@Qualifier(Constants.JOB_ID_IMPORT_OAI + ":importRecordsStep") Step importRecordsStep,
			@Qualifier(Constants.JOB_ID_IMPORT + ":afterHarvestStep") Step afterHarvestStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_OAI)
				.validator(new ImportOaiRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(importRecordsStep)
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(afterHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_OAI + ":importRecordsStep")
	public Step importOaiRecordsStep() throws Exception {
		ItemReader<List<OAIRecord>> reader;
		if (this.asyncReader) {
			reader = asyncImportOaiRecordsReader(STRING_OVERRIDEN_BY_EXPRESSION);
		} else {
			reader = importOaiRecordsReader(STRING_OVERRIDEN_BY_EXPRESSION);
		}
		return steps.get(Constants.JOB_ID_IMPORT_OAI + "importRecordsStep")
				.listener(new StepProgressListener())
				.<List<OAIRecord>, List<HarvestedRecord>>chunk(1)//
				.reader(reader)//
				.processor(oaiItemProcessor())
				.writer(harvestedRecordWriter()) //
				.build();
	}

	// Oai cosmotron format
	@Bean
	public Job OaiImportCosmotronRecordsJob(
			@Qualifier(Constants.JOB_ID_IMPORT_OAI_COSMOTRON + ":importRecordsStep") Step importRecordsStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_OAI_COSMOTRON)
				.validator(new ImportOaiRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(importRecordsStep)
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_OAI_COSMOTRON + ":importRecordsStep")
	public Step importOaiCosmotronRecordsStep() throws Exception {
		return steps.get(Constants.JOB_ID_IMPORT_OAI_COSMOTRON + "importRecordsStep")
				.listener(new StepProgressListener())
				.<List<OAIRecord>, List<HarvestedRecord>>chunk(1)//
				.reader(importOaiRecordsReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.processor(oaiItemProcessor())
				.writer(cosmotronRecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	/**
	 * filename format:
	 * 1) /directory/file.txt - takes file file.txt
	 * 2) /directory/ - takes all files from directory
	 *
	 * @param filename name of input file
	 * @return {@link ImportOaiRecordsFileReader}
	 */
	@Bean(name = Constants.JOB_ID_IMPORT_OAI + ":importRecordsReader")
	@StepScope
	public ItemReader<List<OAIRecord>> importOaiRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
		return new ImportOaiRecordsFileReader(filename);
	}

	/**
	 * filename format:
	 * 1) /directory/file.txt - takes file file.txt
	 * 2) /directory/ - takes all files from directory
	 *
	 * @param filename name of input file
	 * @return {@link AsyncImportOaiRecordsFileReader}
	 */
	@Bean(name = Constants.JOB_ID_IMPORT_OAI + ":asyncImportRecordsReader")
	@StepScope
	public AsyncImportOaiRecordsFileReader asyncImportOaiRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename) throws FileNotFoundException {
		return new AsyncImportOaiRecordsFileReader(filename);
	}

	@Bean(name = Constants.JOB_ID_IMPORT_OAI + ":processor")
	@StepScope
	public OAIItemProcessor oaiItemProcessor() {
		return new OAIItemProcessor();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_OAI + ":HarvestedRecordWriter")
	@StepScope
	public ItemWriter<List<HarvestedRecord>> harvestedRecordWriter() {
		return new HarvestedRecordWriter();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_COSMOTRON + ":cosmotronRecordsWriter")
	@StepScope
	public CosmotronRecordWriter cosmotronRecordsWriter(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId) {
		return new CosmotronRecordWriter(configId);
	}

	// import cosmotron 996
	@Bean
	public Job importCosmotron996RecordsJob(
			@Qualifier(Constants.JOB_ID_IMPORT_COSMOTRON_996 + ":importRecordsStep") Step importRecordsStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_COSMOTRON_996)
				.validator(new ImportRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(importRecordsStep)
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_COSMOTRON_996 + ":importRecordsStep")
	public Step importCosmotron996RecordsStep() throws Exception {
		return steps.get(Constants.JOB_ID_IMPORT_COSMOTRON_996 + "importRecordsStep")
				.listener(new StepProgressListener())
				.<List<Record>, List<Record>>chunk(20)//
				.reader(importRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importCosmotron996RecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_COSMOTRON_996 + ":writer")
	@StepScope
	public ImportCosmotron996RecordsWriter importCosmotron996RecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId) {
		return new ImportCosmotron996RecordsWriter(configurationId);
	}

	// import tezaurus
	@Bean
	public Job importTezaurusRecordsJob(
			@Qualifier(Constants.JOB_ID_IMPORT_TEZAURUS + ":importRecordsStep") Step importRecordsStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_TEZAURUS)
				.validator(new ImportRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(importRecordsStep)
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_TEZAURUS + ":importRecordsStep")
	public Step importTezaurusRecordsStep() throws Exception {
		return steps.get(Constants.JOB_ID_IMPORT_TEZAURUS + "importRecordsStep")
				.listener(new StepProgressListener())
				.<List<Record>, List<Record>>chunk(20)//
				.reader(importRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importTezaurusRecordsWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_TEZAURUS + ":writer")
	@StepScope
	public ItemWriter<List<Record>> importTezaurusRecordsWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId) {
		return new ImportTezaurusRecordsWriter(configurationId);
	}

	// import palmknihy
	@Bean
	public Job ImportPalmknihyJob(
			@Qualifier(Constants.JOB_ID_IMPORT_PALMKNIHY + ":importPalmknihyStep") Step importPalmknihyStep,
			@Qualifier(Constants.JOB_ID_IMPORT + ":afterHarvestStep") Step afterHarvestStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_PALMKNIHY)
				.validator(new ImportRecordsJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE)
				.listener(JobFailureListener.INSTANCE)
				.flow(importPalmknihyStep)
				.next(ReharvestJobExecutionDecider.INSTANCE).on(ReharvestJobExecutionDecider.REHARVEST_FLOW_STATUS.toString()).to(afterHarvestStep) //
				.from(ReharvestJobExecutionDecider.INSTANCE).on(FlowExecutionStatus.COMPLETED.toString()).end() //
				.end().build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_PALMKNIHY + ":importPalmknihyStep")
	public Step importPalmknihyStep() throws Exception {
		return steps.get("importRecordsStep")
				.listener(new StepProgressListener())
				.<List<Record>, List<Record>>chunk(20)//
				.reader(importRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(importPalmknihyWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_IMPORT_PALMKNIHY + ":writer")
	@StepScope
	public ImportPalmknihyWriter importPalmknihyWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId) {
		return new ImportPalmknihyWriter(configurationId);
	}

}
