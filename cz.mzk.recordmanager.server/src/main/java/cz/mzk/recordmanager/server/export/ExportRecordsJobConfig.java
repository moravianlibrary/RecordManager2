package cz.mzk.recordmanager.server.export;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import cz.mzk.recordmanager.server.export.sfx.ExportSfxRecordsJobParametersValidator;
import cz.mzk.recordmanager.server.export.sfx.ExportSfxRecordsProcessor;
import cz.mzk.recordmanager.server.export.sfx.ExportSfxRecordsWriter;
import cz.mzk.recordmanager.server.jdbc.StringValueRowMapper;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
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

	private static final String prepareTempDnntSql = ResourceUtils.asString("job/exportRecordsJob/prepareTempDnnt.sql");

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
				.reader(exportRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION, LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION)) //
				.processor(exportRecordsProcessor(STRING_OVERRIDEN_BY_EXPRESSION)) //
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
			@Value("#{jobParameters[" + Constants.JOB_PARAM_RECORD_IDS + "]}") String recordIds)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id" +
				(deleted == null ? " AND deleted IS NULL" : "") +
				(recordIds != null ? " AND record_id IN (:record_id)" : "")
		);
		pqpf.setSortKey("record_id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", configId);
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
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id");
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
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat) {
		IOFormat iOFormat = IOFormat
				.stringToExportFormat(strFormat);
		return new ExportRecordsProcessor(iOFormat);
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
	public FlatFileItemWriter<String> exportRecordsWriter(@Value("#{jobParameters["
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

	// dnnt
	@Bean
	public Job exportDnntJob(
			@Qualifier(Constants.JOB_ID_EXPORT_DNNT + ":exportDnntStep") Step exportDnntStep) {
		return jobs.get(Constants.JOB_ID_EXPORT_DNNT)
				.validator(new ExportDnntJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
//				.start(prepareTempDnntStep())
				.start(exportDnntStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_DNNT + ":prepareTempDnntTasklet")
	@StepScope
	public Tasklet prepareTempDnntTasklet() {
		return new SqlCommandTasklet(prepareTempDnntSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDnntStep")
	public Step prepareTempDnntStep() {
		return steps.get("prepareTempDnntStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempDnntTasklet())
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_DNNT + ":exportDnntStep")
	public Step exportDnntStep() throws Exception {
		return steps.get("exportDnntStep")
				.listener(new StepProgressListener())
				.<String, String>chunk(100)//
				.reader(dnntReader("tmp_dnnt_map")) //
				.writer(exportDnntWrite(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = Constants.JOB_ID_EXPORT_DNNT + ":exportDnntProcesor")
	@StepScope
	public exportDnntWriter exportDnntWrite(
			@Value("#{jobParameters["+ Constants.JOB_PARAM_OUT_FILE + "]}") String filename) {
		return new exportDnntWriter(filename);
	}

	@StepScope
	public ItemReader<String> dnntReader(String tablename) throws Exception {
		JdbcPagingItemReader<String> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT record_id");
		pqpf.setFromClause("FROM " + tablename);
		pqpf.setSortKey("record_id");
		reader.setRowMapper(new StringValueRowMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

}
