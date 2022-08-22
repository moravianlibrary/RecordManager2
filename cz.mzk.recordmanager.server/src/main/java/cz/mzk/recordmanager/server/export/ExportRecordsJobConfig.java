package cz.mzk.recordmanager.server.export;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import cz.mzk.recordmanager.server.export.sfx.ExportSfxRecordsJobParametersValidator;
import cz.mzk.recordmanager.server.export.sfx.ExportSfxRecordsProcessor;
import cz.mzk.recordmanager.server.export.sfx.ExportSfxRecordsWriter;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import cz.mzk.recordmanager.server.jdbc.Cosmotron996RowMapper;
import cz.mzk.recordmanager.server.jdbc.DedupRecordRowMapper;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class ExportRecordsJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TaskExecutor taskExecutor;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job exportRecordsJob(
			@Qualifier("exportRecordsJob:exportRecordsStep") Step exportRecordsStep) {
		return jobs.get(Constants.JOB_ID_EXPORT)
				.validator(new ExportRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(exportRecordsStep)
				.end()
				.build();
	}

	@Bean
	public Job exportRecordsForClassifierJob(
			@Qualifier("exportRecordsForClassifierJob:exportRecordsForClassifierStep") Step exportRecordsForClassifierStep) {
		return jobs.get(Constants.JOB_ID_EXPORT_RECORDS_FOR_CLASSIFIER)
				.validator(new ExportRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(exportRecordsForClassifierStep)
				.end()
				.build();
	}

	@Bean
	public Job exportCosmotron996Job(
			@Qualifier(Constants.JOB_ID_EXPORT_COSMOTRON_996 + ":exportCosmotron996Step") Step exportCosmotron996Step) {
		return jobs.get(Constants.JOB_ID_EXPORT_COSMOTRON_996)
				.validator(new ExportRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(exportCosmotron996Step)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT + ":exportRecordsStep")
	public Step exportRecordsStep() throws Exception {
		return steps.get("exportRecordsStep")
				.listener(new StepProgressListener())
				.<HarvestedRecordUniqueId, String>chunk(20)//
				.reader(exportRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION, LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION,
						DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION)) //
				.processor(exportRecordsProcessor(STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(exportRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = "exportRecordsForClassifierJob:exportRecordsForClassifierStep")
	public Step exportRecordsForClassifierStep() throws Exception {
		return steps.get("updateRecordsJobStep")
				.<HarvestedRecordUniqueId, String>chunk(20)//
				.reader(exportRecordsForClassifierReader(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.processor(exportRecordsForClassifierProcessor(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(exportRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_COSMOTRON_996 + ":exportCosmotron996Step")
	public Step exportCosmotron996Step() throws Exception {
		return steps.get("exportCosmotron996Step")
				.listener(new StepProgressListener())
				.<Cosmotron996, String>chunk(20)//
				.reader(exportCosmotron996Reader(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.processor(exportCosmotron996Processor(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(exportRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = "exportRecordsJob:exportRecordsReader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> exportRecordsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_DELETED + "]}") Long deleted,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_IDS + "]}") String recordIds,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_HARVESTED_FROM_DATE + "]}") Date harvestedFrom,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_HARVESTED_TO_DATE + "]}") Date harvestedTo)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id" +
				(deleted == null ? " AND deleted IS NULL" : "") +
				(recordIds != null ? " AND record_id IN (:record_id)" : "") +
				(harvestedFrom != null ? " AND harvested > :harvestedFrom" : "") +
				(harvestedTo != null ? " AND harvested < :harvestedTo" : "")
		);
		pqpf.setSortKey("record_id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", configId);
		parameterValues.put("harvestedFrom", harvestedFrom);
		parameterValues.put("harvestedTo", harvestedTo);
		if (recordIds != null) parameterValues.put("record_id", Arrays.asList(recordIds.split(",")));
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = "exportRecordsForClassifierJob:exportRecordsForClassifierReader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> exportRecordsForClassifierReader(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id AND  dedup_record_id IN ( " +
				"  SELECT dedup_record_id " +
				"  FROM harvested_record " +
				"  WHERE EXISTS( " +
				"      SELECT 1 " +
				"      FROM fulltext_kramerius " +
				"      WHERE harvested_record.id = fulltext_kramerius.harvested_record_id " +
				"  ))");
		pqpf.setSortKey("record_id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", configId);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_EXPORT_COSMOTRON_996 + ":exportCosmotron996Reader")
	@StepScope
	public ItemReader<Cosmotron996> exportCosmotron996Reader(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId)
			throws Exception {
		JdbcPagingItemReader<Cosmotron996> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM cosmotron_996");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id AND deleted is null");
		pqpf.setSortKey("record_id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", configId);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new Cosmotron996RowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = "exportRecordsJob:exportRecordsProcesor")
	@StepScope
	public ExportRecordsProcessor exportRecordsProcessor(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_INDEXED_FORMAT + "]}") String indexedFormat) {
		IOFormat iOFormat = IOFormat.stringToExportFormat(strFormat);
		return new ExportRecordsProcessor(iOFormat, indexedFormat != null && indexedFormat.equals("true"));
	}

	@Bean(name = "exportRecordsForClassifierJob:exportRecordsForClassifierProcessor")
	@StepScope
	public ExportRecordsForClassifierProcessor exportRecordsForClassifierProcessor(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat) {
		IOFormat iOFormat = IOFormat
				.stringToExportFormat(strFormat);
		return new ExportRecordsForClassifierProcessor(iOFormat);
	}

	@Bean(name = Constants.JOB_ID_EXPORT_COSMOTRON_996 + ":exportCosmotron996Procesor")
	@StepScope
	public ExportCosmotron996Processor exportCosmotron996Processor(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat) {
		IOFormat iOFormat = IOFormat
				.stringToExportFormat(strFormat);
		return new ExportCosmotron996Processor(iOFormat);
	}

	@Bean(name = "exportRecordsJob:exportRecordsWriter")
	@StepScope
	public synchronized FlatFileItemWriter<String> exportRecordsWriter(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_OUT_FILE + "]}") String filename) throws Exception {
		FlatFileItemWriter<String> fileWritter = new FlatFileItemWriter<>();
		fileWritter.setAppendAllowed(false);
		fileWritter.setShouldDeleteIfExists(true);
		fileWritter.setEncoding("UTF-8");
		fileWritter.setLineAggregator(new PassThroughLineAggregator<>());
		fileWritter.setResource(new FileSystemResource(filename));
		fileWritter.afterPropertiesSet();
		return fileWritter;
	}

	// sfx
	@Bean
	public Job exportSfxRecordsJob(
			@Qualifier(Constants.JOB_ID_EXPORT_SFX + ":exportSfxRecordsStep") Step exportSfxRecordsStep) {
		return jobs.get(Constants.JOB_ID_EXPORT_SFX)
				.validator(new ExportSfxRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(exportSfxRecordsStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_SFX + ":exportSfxRecordsStep")
	public Step exportSfxRecordsStep() throws Exception {
		return steps.get("exportSfxRecordsStep")
				.listener(new StepProgressListener())
				.<DedupRecord, String>chunk(20)//
				.reader(exportSfxRecordsReader()) //
				.processor(exportSfxRecordsProcessor()) //
				.writer(exportSfxRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_SFX + ":exportSfxRecordsReader")
	@StepScope
	public ItemReader<DedupRecord> exportSfxRecordsReader() throws Exception {
		JdbcPagingItemReader<DedupRecord> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT DISTINCT dedup_record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setSortKey("dedup_record_id");
		reader.setRowMapper(new DedupRecordRowMapper("dedup_record_id"));
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_EXPORT_SFX + ":exportRecordsProcesor")
	@StepScope
	public ExportSfxRecordsProcessor exportSfxRecordsProcessor() {
		return new ExportSfxRecordsProcessor();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_SFX + ":exportSfxRecordsWriter")
	@StepScope
	public ExportSfxRecordsWriter exportSfxRecordsWriter(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_OUT_FILE + "]}") String filename) throws Exception {
		return new ExportSfxRecordsWriter(filename);
	}

	//
	@Bean
	public Job exportDuplicityJob(
			@Qualifier(Constants.JOB_ID_EXPORT_DUPLICITY + ":exportDuplicityStep") Step exportDuplicityStep) {
		return jobs.get(Constants.JOB_ID_EXPORT_DUPLICITY)
				.validator(new ExportRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(exportDuplicityStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_DUPLICITY + ":exportDuplicityStep")
	public Step exportDuplicityStep() throws Exception {
		return steps.get("exportRecordsStep")
				.listener(new StepProgressListener())
				.<HarvestedRecordUniqueId, String>chunk(50)//
				.reader(exportDuplicityReader(LONG_OVERRIDEN_BY_EXPRESSION, LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
				.processor(exportRecordsProcessor(STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(exportRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_DUPLICITY + ":exportDuplicityReader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> exportDuplicityReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_DELETED + "]}") Long deleted,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_IDS + "]}") String recordIds)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id= :conf_id and deleted is null and dedup_record_id in " +
				"(select dedup_record_id from harvested_record where import_conf_id= :conf_id and deleted is null " +
				"GROUP BY dedup_record_id HAVING count(*) > 1)"
		);
		pqpf.setSortKey("record_id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", configId);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(5000);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	// export marc fields
	@Bean
	public Job exportMarcFieldsJob(
			@Qualifier(Constants.JOB_ID_EXPORT_MARC_FIELDS + ":exportMarcFieldsStep") Step exportMarcFieldsStep) {
		return jobs.get(Constants.JOB_ID_EXPORT_MARC_FIELDS)
				.validator(new ExportMarcFieldsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(exportMarcFieldsStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_MARC_FIELDS + ":exportMarcFieldsStep")
	public Step exportMarcFieldsStep() throws Exception {
		return steps.get("exportMarcFieldsStep")
				.listener(new StepProgressListener())
				.<Long, String>chunk(200)//
				.reader(exportMarcFieldsReader(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.processor(exportMarcFieldsProcesor(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(exportRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_MARC_FIELDS + ":exportMarcFieldsReader")
	@StepScope
	public synchronized ItemReader<Long> exportMarcFieldsReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") String configId)
			throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record");
		if (configId != null) {
			Map<String, Object> parameterValues = new HashMap<>();
			pqpf.setWhereClause("WHERE import_conf_id IN (:conf_id)");
			parameterValues.put("conf_id", Arrays.stream(configId.split(",")).map(Long::parseLong).collect(Collectors.toList()));
			reader.setParameterValues(parameterValues);
		}
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(200);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_EXPORT_MARC_FIELDS + ":exportMarcFieldsProcesor")
	@StepScope
	public ExportMarcFieldsProcessor exportMarcFieldsProcesor(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FIELDS + "]}") String marcFields) {
		return new ExportMarcFieldsProcessor(marcFields);
	}

}
