package cz.mzk.recordmanager.server.miscellaneous;

import cz.mzk.recordmanager.server.export.HarvestedRecordIdRowMapper;
import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.miscellaneous.caslin.keys.*;
import cz.mzk.recordmanager.server.miscellaneous.itemid.GenerateItemIdJobParametersValidator;
import cz.mzk.recordmanager.server.miscellaneous.itemid.GenerateItemIdWriter;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.harvest.HarvestedRecordWriter;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Configuration
public class MiscellaneousJobsConfig {

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

	@Bean
	public Job generateSkatKeysJob(
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep") Step generateSkatKeysStep,
			@Qualifier(Constants.JOB_ID_MANUALLY_MERGED_SKAT_DEDUP_KEYS + ":generateManuallyMergedSkatKeysStep") Step generateManuallyMergedSkatKeysStep
	) {
		return jobs.get(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS)
				.validator(new GenerateSkatKeysJobParameterValidator())
				.start(generateSkatKeysStep)
				.next(generateManuallyMergedSkatKeysStep)
				.build();
	}

	@Bean
	public Job generateLocalSkatDedupKeysJob(
			@Qualifier(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep") Step generateSkatKeysStep) {
		return jobs.get(Constants.JOB_ID_GENERATE_LOCAL_SKAT_DEDUP_KEYS)
				.validator(new GenerateSkatKeysJobParameterValidator())
				.start(generateSkatKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":updateStatMergedIdsStep")
	@Deprecated
	public Step updateStatMergedIdsStep() throws Exception {
		return steps.get("updateStatMergedIdsStep")
				.tasklet(updateStatMergedIdsStepTasklet(DATE_OVERRIDEN_BY_EXPRESSION))
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "updateStatMergedIdsStep:updateStatMergedIdsTasklet")
	@StepScope
	public Tasklet updateStatMergedIdsStepTasklet(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date fromDate) {
		return new SkatKeysMergedIdsUpdateTasklet(fromDate);
	}

	// generateManuallyMergedSkatDedupKeys
	@Bean
	public Job generateManuallyMergedSkatDedupKeysJob(
			@Qualifier(Constants.JOB_ID_MANUALLY_MERGED_SKAT_DEDUP_KEYS + ":generateManuallyMergedSkatKeysStep") Step generateManuallyMergedSkatKeysStep) {
		return jobs.get(Constants.JOB_ID_MANUALLY_MERGED_SKAT_DEDUP_KEYS)
				.validator(new GenerateSkatKeysJobParameterValidator())
				.start(generateManuallyMergedSkatKeysStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_MANUALLY_MERGED_SKAT_DEDUP_KEYS + ":generateManuallyMergedSkatKeysStep")
	public Step generateManuallyMergedSkatKeysStep() throws Exception {
		return steps.get("generateManuallyMergedSkatKeysStep")
				.listener(new StepProgressListener())
				.<Set<SkatKey>, Set<SkatKey>>chunk(1)
				.reader(generateManuallyMergedSkatKeysReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION))
				.writer(generateSkatKeysWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_MANUALLY_MERGED_SKAT_DEDUP_KEYS + ":generateManuallyMergedSkatKeysReader")
	@StepScope
	public ItemReader<? extends Set<SkatKey>> generateManuallyMergedSkatKeysReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date fromDate,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date toDate) {
		return new ManuallyMergedSkatDedupKeysReader(fromDate, toDate);
	}

	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysStep")
	public Step generateSkatKeysStep() throws Exception {
		return steps.get("generateSkatKeysStep")
				.listener(new StepProgressListener())
				.<Long, Set<SkatKey>>chunk(100)
				.reader(generateSkatKeysReader(DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION))
				.processor(generateSkatKeysProcessor())
				.writer(generateSkatKeysWriter())
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysReader")
	@StepScope
	public ItemReader<Long> generateSkatKeysReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FROM_DATE + "]}") Date fromDate,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE + "]}") Date toDate)
			throws Exception {
		Date from = fromDate == null ? new Date(0) : fromDate;
		Date to = toDate == null ? new Date() : toDate;
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id AND updated > :updated_from AND updated < :updated_to");
		pqpf.setSortKey("id");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", Constants.IMPORT_CONF_ID_CASLIN);
		parameterValues.put("updated_from", from);
		parameterValues.put("updated_to", to);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysProcessor")
	@StepScope
	public GenerateSkatKeysProcessor generateSkatKeysProcessor() {
		return new GenerateSkatKeysProcessor();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS + ":generateSkatKeysWriter")
	@StepScope
	public GenerateSkatKeysWriter generateSkatKeysWriter() {
		return new GenerateSkatKeysWriter();
	}

	// generateItemIdJob
	@Bean
	public Job generateItemIdJob(
			@Qualifier(Constants.JOB_ID_GENERATE_ITEM_ID + ":generateItemIdStep") Step generateItemIdStep) {
		return jobs.get(Constants.JOB_ID_GENERATE_ITEM_ID)
				.validator(new GenerateItemIdJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(generateItemIdStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_ITEM_ID + ":generateItemIdStep")
	public Step generateItemIdStep() throws Exception {
		return steps.get("generateItemIdStep")
				.listener(new StepProgressListener())
				.<HarvestedRecordUniqueId, HarvestedRecordUniqueId>chunk(20)//
				.reader(generateItemIdReader(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.writer(generateItemIdWriter()) //
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_GENERATE_ITEM_ID + ":generateItemIdReader")
	@StepScope
	public synchronized ItemReader<HarvestedRecordUniqueId> generateItemIdReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long confId)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		String where = "WHERE deleted is null";
		if (confId != null) {
			where += " AND import_conf_id=:conf_id";
			Map<String, Object> parameterValues = new HashMap<>();
			parameterValues.put("conf_id", confId);
			reader.setParameterValues(parameterValues);
		}
		pqpf.setWhereClause(where);
		pqpf.setSortKey("record_id");

		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = Constants.JOB_ID_GENERATE_ITEM_ID + ":generateItemIdWriter")
	@StepScope
	public GenerateItemIdWriter generateItemIdWriter() {
		return new GenerateItemIdWriter();
	}

	//
	@Bean
	public Job MappedSourceFilterJob(
			@Qualifier(Constants.JOB_ID_MAPPED_SOURCE_FILTER + ":mappedSourceFilterStep") Step mappedSourceFilterStep
	) {
		return jobs.get(Constants.JOB_ID_MAPPED_SOURCE_FILTER)
				.validator(new MappedSourceJobParametersValidator())
				.flow(mappedSourceFilterStep)
				.end()
				.build();
	}

	@Bean(name = Constants.JOB_ID_MAPPED_SOURCE_FILTER + ":mappedSourceFilterStep")
	public Step mappedSourceFilterStep() throws Exception {
		return steps.get("mappedSourceFilterStep")
				.listener(new StepProgressListener())
				.<List<String>, List<HarvestedRecord>>chunk(100)
				.reader(mappedSourceFilterReader(LONG_OVERRIDEN_BY_EXPRESSION))
				.processor(mappedSourceFilterProcessor())
				.writer(mappedSourceFilterWriter())
				.taskExecutor(taskExecutor)
				.build();
	}

	@Bean(name = Constants.JOB_ID_MAPPED_SOURCE_FILTER + ":mappedSourceFilterReader")
	@StepScope
	public synchronized ItemReader<List<String>> mappedSourceFilterReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long confId)
			throws Exception {
		JdbcPagingItemReader<List<String>> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id=:conf_id AND deleted is null");
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("conf_id", confId);
		reader.setParameterValues(parameterValues);
		pqpf.setSortKey("record_id");

		reader.setRowMapper(new ArrayHarvestedRecordMapper());
		reader.setPageSize(1000);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}


	@Bean(name = Constants.JOB_ID_MAPPED_SOURCE_FILTER + ":mappedSourceFilterProcessor")
	@StepScope
	public MappedSourceFilterProcessor mappedSourceFilterProcessor() {
		return new MappedSourceFilterProcessor();
	}

	@Bean(name = Constants.JOB_ID_MAPPED_SOURCE_FILTER + ":mappedSourceFilterWriter")
	@StepScope
	public HarvestedRecordWriter mappedSourceFilterWriter() {
		return new HarvestedRecordWriter();
	}

	public static class ArrayHarvestedRecordMapper implements RowMapper<List<String>> {

		@Override
		public List<String> mapRow(ResultSet rs, int arg1) throws SQLException {
			return Arrays.asList(rs.getString("record_id").split(","));
		}
	}

}
