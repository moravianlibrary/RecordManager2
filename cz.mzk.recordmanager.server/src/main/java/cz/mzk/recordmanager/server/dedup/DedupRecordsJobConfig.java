package cz.mzk.recordmanager.server.dedup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.io.CharStreams;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.DelegatingHibernateProcessor;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class DedupRecordsJobConfig {

	private static final String TMP_TABLE_ISBN = "tmp_simmilar_books_isbn";

	private static final String TMP_TABLE_CNB = "tmp_simmilar_books_cnb";

	private static final String TMP_TABLE_CLUSTER = "tmp_cluster_ids";

	private static final String TMP_TABLE_AUTH_TITLE = "tmp_auth_keys";

	private static final String TMP_TABLE_REST_OF_IDS_INTERVALS = "tmp_rest_of_ids_intervals";
	
	private static final String TMP_TABLE_CNB_CLUSTERS = "tmp_cnb_clusters";
	
	private static final String TMP_TABLE_OCLC_CLUSTERS = "tmp_oclc_clusters";
	
	private static final String TMP_TABLE_UUID_CLUSTERS = "tmp_uuid_clusters";
	
	private static final String TMP_TABLE_SIMILARITY_IDS = "tmp_similarity_ids";
	
	private static final String TMP_TABLE_SKAT_KEYS_MANUALLY_MERGED = "tmp_skat_keys_manually_merged";
	
	private static final String TMP_TABLE_SKAT_KEYS_REST = "tmp_skat_keys_rest";
	
	private static final String PREPARE_REST_OF_RECORDS_TABLE_PROCEDURE = "prepare_rest_of_ids_table";

	private static final String PREPARE_REST_OF_RECORDS_COMMIT_OFFSETS_PROCEDURE = "dedup_rest_of_records_offset";

	private static final int REST_OF_RECORDS_COMMIT_INTERVAL = 10000;
	
	
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TaskExecutor taskExecutor;


	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	private String initDeduplicationSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/initDeduplication.sql"),
					"UTF-8"));

	private String prepareTempIsbnTableSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempIsbnTable.sql"),
					"UTF-8"));

	private String prepareTempCnbTableSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempCnbTable.sql"),
					"UTF-8"));

	private String prepareTempClusterIdSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempClusterId.sql"),
					"UTF-8"));

	private String prepareTempAuthKeyTableSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempAuthKeyTable.sql"),
					"UTF-8"));
	
	private String prepareTempCnbClustersSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempCnbClustersTable.sql"),
					"UTF-8"));

	private String prepareTempOclcClustersSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempOclcClustersTable.sql"),
					"UTF-8"));
	
	private String prepareTempUuidClustersSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempUuidClustersTable.sql"),
					"UTF-8"));
	
	private String prepareDedupSimmilarityTableSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareDedupSimmilarityTable.sql"),
					"UTF-8"));
	
	private String prepareTempSkatKeysManuallyMerged = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempSkatManuallyMergedTable.sql"),
					"UTF-8"));
	
	private String prepareTempSkatKeysRest = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempSkatRestTable.sql"),
					"UTF-8"));
	
	private String cleanupSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/cleanup.sql"),
					"UTF-8"));
	

	public DedupRecordsJobConfig() throws IOException {
	}

	@Bean
	public Job dedupRecordsJob(
			// public Job
			// dedupRecordsJob(@Qualifier("dedupRecordsJob:deleteStep") Step
			// deleteStep,
			// @Qualifier("dedupRecordsJob:updateStep") Step updateStep) {
			// @Qualifier("dedupRecordsJob:deleteStep") Step deleteStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":initStep") Step initStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempClusterIdStep") Step prepareTempClusterIdStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupClusterIdsStep") Step dedupClusterIdsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempSkatKeysManuallyMergedStep") Step prepareTempSkatKeysManuallyMergedStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatManuallyMergedStep") Step dedupSimpleKeysSkatManuallyMergedStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempIsbnTableStep") Step prepareTempIsbnTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnStep") Step dedupSimpleKeysISBNStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempCnbTableStep") Step prepareTempCnbTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbStep") Step dedupSimpleKeysCnbStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTmpTitleAuthStep") Step prepareTmpTitleAuthStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupTitleAuthStep") Step dedupTitleAuthStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempCnbClustersTableStep") Step prepareTempCnbClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupCnbClustersStep") Step dedupCnbClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempOclcClustersTableStep") Step prepareTempOclcClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupOclcClustersStep") Step dedupOclcClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempUuidClustersTableStep") Step prepareTempUuidClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupUuidClustersStep") Step dedupUuidClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempSkatKeysRestStep") Step prepareTempSkatKeysRestStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatRestStep") Step dedupSimpleKeysSkatRestStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupSimmilarityTableStep") Step prepareDedupSimmilarityTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupSimmilarTitlesStep") Step prepareDedupSimmilarTitles,
			@Qualifier(Constants.JOB_ID_DEDUP + ":processSimilaritesResultsStep") Step processSimilaritesResultsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupRestOfRecordsStep") Step prepareDedupRestOfRecordsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupRestOfRecordsStep") Step dedupRestOfRecordsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":cleanupStep") Step cleanupStep) {
		return jobs.get(Constants.JOB_ID_DEDUP)
				.validator(new DedupRecordsJobParametersValidator())
				.start(initStep)
				.next(prepareTempClusterIdStep)
				.next(dedupClusterIdsStep)
				.next(prepareTempSkatKeysManuallyMergedStep)
				.next(dedupSimpleKeysSkatManuallyMergedStep)
				.next(prepareTempIsbnTableStep)
				.next(dedupSimpleKeysISBNStep)
				.next(prepareTempCnbTableStep)
				.next(dedupSimpleKeysCnbStep)
				.next(prepareTmpTitleAuthStep)
				.next(dedupTitleAuthStep)
				.next(prepareTempCnbClustersTableStep)
				.next(dedupCnbClustersStep)
				.next(prepareTempOclcClustersTableStep)
				.next(dedupOclcClustersStep)
				.next(prepareTempUuidClustersTableStep)
				.next(dedupUuidClustersStep)
				.next(prepareTempSkatKeysRestStep)
				.next(dedupSimpleKeysSkatRestStep)
				.next(prepareDedupSimmilarityTableStep)
				.next(prepareDedupSimmilarTitles)
				.next(processSimilaritesResultsStep)
				.next(prepareDedupRestOfRecordsStep)
				.next(dedupRestOfRecordsStep)
				.next(cleanupStep)
				.build();
	}

	// @Bean(name="dedupRecordsJob:deleteStep")
	// public Step deleteStep() throws Exception {
	// return steps.get("dedupRecordsStep")
	// .tasklet(updateDedupRecordTasklet())
	// .build();
	// }
	//
	// @Bean(name="dedupRecordsJob:updateStep")
	// public Step step() throws Exception {
	// return steps.get("dedupRecordsUpdateStep")
	// .<HarvestedRecord, HarvestedRecord> chunk(100)
	// .reader(reader())
	// .writer(writer())
	// .build();
	// }
	//
	
	/*
	 *
	 * @return
	 */
	/*
	 * initialize deduplication
	 */
	@Bean(name="prepareTempTablesStep:initTasklet")
	@StepScope
	public Tasklet initTasklet() {
		return new SqlCommandTasklet(initDeduplicationSql);
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":initStep")
	public Step initStep() {
		return steps.get("initTasklet")
				.tasklet(initTasklet())
				.listener(new StepProgressListener())
				.build();
	}
	

	/*
	 * dedupClusterIdsStep Deduplicate records using cluster id
	 */

	@Bean(name = "prepareTempTablesStep:prepareTempClusterIdTasklet")
	@StepScope
	public Tasklet prepareTempClusterIdTasklet() {
		return new SqlCommandTasklet(prepareTempClusterIdSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempClusterIdStep")
	public Step prepareTempClusterIdTableStep() {
		return steps.get("prepareTempClusterIdStep")
				.tasklet(prepareTempClusterIdTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupClusterIdsStep")
	public Step dedupClusterIdsStep() throws Exception {
		return steps.get("dedupClusterIdsStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupClusterIdReader())
				.processor(dedupSkatKeysProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = "dedupClusterId:reader")
	@StepScope
	public ItemReader<List<Long>> dedupClusterIdReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_CLUSTER);
	}

/*
 * dedupSimpleKeysSkatManuallyMergedStep Deduplicate all records, that were manually
 * merged in Skat
 * 
 */
	@Bean(name = "prepareTempSkatKeysManuallyMergedStep:prepareTempSkatKeysManuallyMergedTasklet")
	@StepScope
	public Tasklet prepareTempSkatKeysManuallyMergedTasklet() {
		return new SqlCommandTasklet(prepareTempSkatKeysManuallyMerged);
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempSkatKeysManuallyMergedStep")
	public Step prepareTempSkatKeysManuallyMergedStep() {
		return steps.get("prepareTempSkatKeysManuallyMergedStep")
				.tasklet(prepareTempSkatKeysManuallyMergedTasklet())
				.listener(new StepProgressListener())
				.build();
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatManuallyMergedStep")
	public Step dedupSimpleKeysSkatManuallyMergedStep() throws Exception {
		return steps.get("dedupSimpleKeysSkatManuallyMergedStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupSimpleKeysSkatManuallyMergedReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}
	
	@Bean(name = "dedupSimpleKeysSkatManuallyMergedStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysSkatManuallyMergedReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_SKAT_KEYS_MANUALLY_MERGED);
	}

	
/*
 * dedupSimpleKeysIsbnStep Deduplicate all books having equal publication
 * year, ISBN and title
 */
	@Bean(name = "prepareTempTablesStep:prepareTempIsbnTableTasklet")
	@StepScope
	public Tasklet prepareTempIsbnTableTasklet() {
		return new SqlCommandTasklet(prepareTempIsbnTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempIsbnTableStep")
	public Step prepareTempIsbnTableStep() {
		return steps.get("prepareTempIsbnTableStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempIsbnTableTasklet()).build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnStep")
	public Step dedupSimpleKeysIsbnStep() throws Exception {
		return steps.get("dedupSimpleKeysIsbnStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupSimpleKeysIsbnReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter()).build();
	}

	@Bean(name = "dedupSimpleKeysIsbnStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysIsbnReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_ISBN);
	}

/*
 * dedupSimpleKeysCnbStep Deduplicate all books having equal publication
 * year, CNB and title
 */
	@Bean(name = "prepareTempTablesStep:prepareTempCnbTableTasklet")
	@StepScope
	public Tasklet prepareTempCnbTableTasklet() {
		return new SqlCommandTasklet(prepareTempCnbTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempCnbTableStep")
	public Step prepareTempCnbnTableStep() {
		return steps.get("prepareTempCnbTableStep")
				.tasklet(prepareTempCnbTableTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbStep")
	public Step dedupSimpleKeysCnbStep() throws Exception {
		return steps.get("dedupSimpleKeysISBNStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupSimpleKeysCnbReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter()).build();

	}

	@Bean(name = "dedupSimpleKeysCnbStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysCnbReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_CNB);
	}

/*
 * dedupTitleAuthStep Deduplicate all books having same title, author key,
 * publication year and page count in tolerance
 */
	@Bean(name = "prepareTempTablesStep:prepareTmpTitkeAuthStepTasklet")
	@StepScope
	public Tasklet prepareTmpTitleAuthStepTasklet() {
		return new SqlCommandTasklet(prepareTempAuthKeyTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTmpTitleAuthStep")
	public Step prepareTmpTitleAuthStep() {
		return steps.get("prepareTmpTitleAuthStep")
				.tasklet(prepareTmpTitleAuthStepTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupTitleAuthStep")
	public Step dedupTitleAuthStep() throws Exception {
		return steps.get("dedupTitleAuthStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupTitleAuthReader())
				.processor(dedupTitleAuthProcessor())
				.writer(dedupSimpleKeysStepWriter()).build();
	}

	@Bean(name = "dedupTitleAuth:reader")
	@StepScope
	public ItemReader<List<Long>> dedupTitleAuthReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_AUTH_TITLE);
	}

	@Bean(name = "dedupTitleAuthStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupTitleAuthProcessor() {
		return new DedupTitleAuthItemProcessor();
	}

/*
 * Deduplicate rest of records. Final step of deduplication.
 * All HarvesteRecords without DedupRecord are assigned unique DedupRecord
 * 
 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareDedupRestOfRecordsStep")
	public Step prepareDedupRestOfRecordsStep() {
		return steps
				.get("prepareDedupRestOfRecordsStep")
				.tasklet(dedupRestOfRecordsSqlTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "prepareTempTablesStep:dedupRestTasklet")
	@StepScope
	public Tasklet dedupRestOfRecordsSqlTasklet() {
		List<String> commands = new ArrayList<>();
		commands.add(String.format("select %s()",
				PREPARE_REST_OF_RECORDS_TABLE_PROCEDURE));
		commands.add(String.format("select %s(%s)",
				PREPARE_REST_OF_RECORDS_COMMIT_OFFSETS_PROCEDURE,
				REST_OF_RECORDS_COMMIT_INTERVAL));
		return new SqlCommandTasklet(commands);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupRestOfRecordsStep")
	public Step dedupRestOfRecordsStep() throws Exception {
		return steps.get("dedupRestOfRecordsStep")
				.listener(new StepProgressListener())
				.<Long, Long> chunk(1).reader(dedupRestOfRecordsReader())
				.writer(dedupRestOfRecordsWriter()).build();
	}

	@Bean(name = "dedupRestOfRecordsStep:reader")
	@StepScope
	public ItemReader<Long> dedupRestOfRecordsReader() throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT interval");
		pqpf.setFromClause("FROM " + TMP_TABLE_REST_OF_IDS_INTERVALS);
		pqpf.setSortKey("interval");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(1);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = "dedupRestOfRecordsStep:writer")
	@StepScope
	public ItemWriter<Long> dedupRestOfRecordsWriter() throws Exception {
		return new ItemWriter<Long>() {

			@Autowired
			private JdbcTemplate jdbcTemplate;

			@Override
			public void write(List<? extends Long> arg0) throws Exception {
				for (Long l : arg0) {
					jdbcTemplate
							.query("select dedup_rest_of_records(?,?)",
									new Object[] {
											REST_OF_RECORDS_COMMIT_INTERVAL, l },
									new LongValueRowMapper());
				}
			}
		};
	}

/*
 * Deduplicate same CNB 
 */
	@Bean(name = "prepareTempTablesStep:prepareTempCnbClustersTableTasklet")
	@StepScope
	public Tasklet prepareCbnClustersTasklet() {
		return new SqlCommandTasklet(prepareTempCnbClustersSql);
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempCnbClustersTableStep")
	public Step prepareTempCnbClustersTableStep() {
		return steps.get("prepareTempCnbClustersTableStep")
				.tasklet(prepareCbnClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}
	
	@Bean(name = "dedupCnbClustersStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupCnbClustersReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_CNB_CLUSTERS);
	}
	
	@Bean(name = "dedupCnbClustersStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupCnbClustersProcessor() {
		return new DedupIdentifierClustersProcessor();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupCnbClustersStep")
	public Step dedupCnbClustersStep() throws Exception {
		return steps.get("dedupCnbClustersTableStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupCnbClustersReader())
				.processor(dedupCnbClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}
	
/*
 * Dedup same Oclc
 */
	@Bean(name = "prepareTempTablesStep:prepareTempOclcClustersTableTasklet")
	@StepScope
	public Tasklet prepareOclcClustersTasklet() {
		return new SqlCommandTasklet(prepareTempOclcClustersSql);
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempOclcClustersTableStep")
	public Step prepareTempOclcClustersTableStep() {
		return steps.get("prepareTempOclcClustersTableStep")
				.tasklet(prepareOclcClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}
	
	@Bean(name = "dedupOclcClustersStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupOclcClustersReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_OCLC_CLUSTERS);
	}
	
	@Bean(name = "dedupOclcClustersStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupOclcClustersProcessor() {
		return new DedupIdentifierClustersProcessor();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupOclcClustersStep")
	public Step dedupOclcClustersStep() throws Exception {
		return steps.get("dedupOclcClustersTableStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupOclcClustersReader())
				.processor(dedupOclcClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}
/*
 * Deduplicate same UUID
 */

	@Bean(name = "prepareTempTablesStep:prepareTempUuidClustersTableTasklet")
	@StepScope
	public Tasklet prepareUuidClustersTasklet() {
		return new SqlCommandTasklet(prepareTempUuidClustersSql);
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempUuidClustersTableStep")
	public Step prepareTempUuidClustersTableStep() {
		return steps.get("prepareTempUuidClustersTableStep")
				.tasklet(prepareUuidClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}
	
	@Bean(name = "dedupUuidClustersStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupUuidClustersReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_UUID_CLUSTERS);
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupUuidClustersStep")
	public Step dedupUuidClustersStep() throws Exception {
		return steps.get("dedupUuidClustersStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupUuidClustersReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}
	
/*
 * dedupSimpleKeysSkatManuallyMergedStep Deduplicate all records, that were NOT manually
 * merged in Skat
 * 
 */
		@Bean(name = "prepareTempSkatKeysRestStep:prepareTempSkatKeysRestTasklet")
		@StepScope
		public Tasklet prepareTempSkatKeysRestTasklet() {
			return new SqlCommandTasklet(prepareTempSkatKeysRest);
		}
		
		@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempSkatKeysRestStep")
		public Step prepareTempSkatKeysRestStep() {
			return steps.get("prepareTempSkatKeysRestStep")
					.tasklet(prepareTempSkatKeysRestTasklet())
					.listener(new StepProgressListener())
					.build();
		}
		
		@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatRestStep")
		public Step dedupSimpleKeysSkatRestStep() throws Exception {
			return steps.get("dedupSimpleKeysSkatRestStep")
					.listener(new StepProgressListener())
					.<List<Long>, List<HarvestedRecord>> chunk(100)
					.reader(dedupSimpleKeysSkatRestReader())
					.processor(dedupSkatKeysProcessor())
					.writer(dedupSimpleKeysStepWriter())
					.build();
		}
		
		@Bean(name = "dedupSimpleKeysSkatRestStep:reader")
		@StepScope
		public ItemReader<List<Long>> dedupSimpleKeysSkatRestReader() throws Exception {
			return dedupSimpleKeysReader(TMP_TABLE_SKAT_KEYS_REST);
		}
	
	
	
	
/*
 * Prepare title similarities deduplication
 */
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareDedupSimmilarityTableStep")
	public Step prepareDedupSimmilarityTableStep() {
		return steps.get("prepareDedupSimmilarityTableStep")
				.tasklet(prepareDedupSimmilarityTable())
				.listener(new StepProgressListener())
				.build();
	}
	
	@Bean(name = "prepareDedupSimmilarityTableStep:prepareDedupSimmilarityTableTasklet")
	@StepScope
	public Tasklet prepareDedupSimmilarityTable() {
		return new SqlCommandTasklet(prepareDedupSimmilarityTableSql);
	}
	
	@Bean(name="prepareDedupSimmilarTitlesStep:yearReader")
	public ItemReader<List<TitleForDeduplication>> yearReader() {
		return new TitleByYearReader();
	}
	
	@Bean(name="prepareDedupSimmilarTitlesStep:titleProcessor")
	public ItemProcessor<List<TitleForDeduplication>,List<Set<Long>>> titleProcessor() {
		return new SimilarTitleProcessor();
	}
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareDedupSimmilarTitlesStep")
	public Step prepareDedupSimmilarTitles() throws Exception {
		return steps.get("prepareDedupSimmilarTitlesStep")
				.listener(new StepProgressListener())
				.<List<TitleForDeduplication>, Future<List<Set<Long>>>> chunk(10)
				.reader(yearReader())
				.processor(asyncSimmilarityProcessor())
				.writer(asyncSimmilarityWriter())
				.build();
	}
	
	@Bean(name ="prepareDedupSimmilarTitlesStep:asynprepareDedupSimmilarTitlesProcessor")
	@StepScope
	public AsyncItemProcessor<List<TitleForDeduplication>, List<Set<Long>>> asyncSimmilarityProcessor() {
		AsyncItemProcessor<List<TitleForDeduplication>, List<Set<Long>>> processor = new AsyncItemProcessor<>();
		processor.setDelegate(new DelegatingHibernateProcessor<>(sessionFactory, titleProcessor()));
		processor.setTaskExecutor(taskExecutor);
		return processor;
	}

	
	@Bean(name="prepareDedupSimmilarTitlesStep:asyncSimmilarityWriter")
	@StepScope
	public AsyncItemWriter<List<Set<Long>>> asyncSimmilarityWriter() throws Exception {
		AsyncItemWriter<List<Set<Long>>> writer = new AsyncItemWriter<List<Set<Long>>>();
		writer.setDelegate(simpleSimmilarityWriter());
		writer.afterPropertiesSet();
		return writer;
	
	}
	
	@Bean(name="prepareDedupSimmilarTitlesStep:simpleSimmilarityWriter")
	@StepScope
	public ItemWriter<List<Set<Long>>> simpleSimmilarityWriter() {
		return new TitleSimilarityWriter();
	}

/*
 * 
 * process computed similarities results
 */
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":processSimilaritesResultsStep")
	public Step processSimilaritesResultsStep() throws Exception {
		return steps.get("processSimilaritesResultsStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(100)
				.reader(dedupSimpleKeysReader(TMP_TABLE_SIMILARITY_IDS))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}
	
	
/*
 * 
 * cleanup
 */
	
	
	@Bean(name = Constants.JOB_ID_DEDUP + ":cleanupStep")
	public Step cleanupStep() {
		return steps.get("cleanupStep")
				.tasklet(cleanupTasklet())
				.listener(new StepProgressListener())
				.build();
	}
	
	@Bean(name = "cleanupStap:cleanupTasklet")
	@StepScope
	public Tasklet cleanupTasklet() {
		return new SqlCommandTasklet(cleanupSql);
	}
	
	
	
	
/*
 * Generic components
 */
	public ItemReader<List<Long>> dedupSimpleKeysReader(String tablename)
			throws Exception {
		JdbcPagingItemReader<List<Long>> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT row_id,id_array");
		pqpf.setFromClause("FROM " + tablename);
		pqpf.setSortKey("row_id");
		reader.setRowMapper(new ArrayLongMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}
	
	@Bean(name = "dedupSimpleKeys:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupSimpleKeysStepProsessor() {
		return new DedupSimpleKeysStepProsessor();
	}

	@Bean(name = "dedupSimpleKeys:writer")
	@StepScope
	public ItemWriter<List<HarvestedRecord>> dedupSimpleKeysStepWriter()
			throws Exception {
		return new DedupSimpleKeysStepWriter();
	}
	
	
	/**
	 * Record processor for deduplication of records based on Skat data
	 * @return
	 */
	@Bean(name = "dedupSkatKeys:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupSkatKeysProcessor() {
		return new DedupSkatKeysProcessor();
	}

	public class ArrayLongMapper implements RowMapper<List<Long>> {

		@Override
		public List<Long> mapRow(ResultSet arg0, int arg1) throws SQLException {
			List<Long> hrs = new ArrayList<>();

			String ids = arg0.getString("id_array");
			for (String idStr : ids.split(",")) {
				Long hrId = Long.valueOf(idStr);
				hrs.add(hrId);
			}

			return hrs;
		}
	}
}
