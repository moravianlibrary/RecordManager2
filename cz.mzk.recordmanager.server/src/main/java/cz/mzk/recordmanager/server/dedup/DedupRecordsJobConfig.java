package cz.mzk.recordmanager.server.dedup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.io.CharStreams;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
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
	
	private static final String TMP_TABLE_UUID_CLUSTERS = "tmp_uuid_clusters";
	
	private static final String PREPARE_REST_OF_RECORDS_TABLE_PROCEDURE = "prepare_rest_of_ids_table";

	private static final String PREPARE_REST_OF_RECORDS_COMMIT_OFFSETS_PROCEDURE = "dedup_rest_of_records_offset";

	private static final int REST_OF_RECORDS_COMMIT_INTERVAL = 10000;


	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	private String initDeduplicationSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/initDeduplication.sql"),
					"UTF-8"));
	
	private String updateDedupRecordSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/updateDedupRecord.sql"),
					"UTF-8"));

	private String deleteRecordLinkSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/deleteRecordLink.sql"),
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
	
	private String prepareTempUuidClustersSql = CharStreams
			.toString(new InputStreamReader(getClass() //
					.getClassLoader().getResourceAsStream(
							"job/dedupRecordsJob/prepareTempUuidClustersTable.sql"),
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
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempIsbnTableStep") Step prepareTempIsbnTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnStep") Step dedupSimpleKeysISBNStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempCnbTableStep") Step prepareTempCnbTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbStep") Step dedupSimpleKeysCnbStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTmpTitleAuthStep") Step prepareTmpTitleAuthStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupTitleAuthStep") Step dedupTitleAuthStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempCnbClustersTableStep") Step prepareTempCnbClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupCnbClustersStep") Step dedupCnbClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempUuidClustersTableStep") Step prepareTempUuidClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupUuidClustersStep") Step dedupUuidClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupRestOfRecordsStep") Step prepareDedupRestOfRecordsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupRestOfRecordsStep") Step dedupRestOfRecordsStep) {
		return jobs.get(Constants.JOB_ID_DEDUP)
				.validator(new DedupRecordsJobParametersValidator())
				.start(initStep)
				.next(prepareTempClusterIdStep)
				.next(dedupClusterIdsStep)
				.next(prepareTempIsbnTableStep)
				.next(dedupSimpleKeysISBNStep)
				.next(prepareTempCnbTableStep)
				.next(dedupSimpleKeysCnbStep)
				.next(prepareTmpTitleAuthStep)
				.next(dedupTitleAuthStep)
				.next(prepareTempCnbClustersTableStep)
				.next(dedupCnbClustersStep)
				.next(prepareTempUuidClustersTableStep)
				.next(dedupUuidClustersStep)
				.next(prepareDedupRestOfRecordsStep)
				.next(dedupRestOfRecordsStep)
				// .next(dropTempTablesStep)
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
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = "dedupClusterId:reader")
	@StepScope
	public ItemReader<List<Long>> dedupClusterIdReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_CLUSTER);
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
		return new DedupCnbClustersProcessor();
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
 * Generic components
 */
	public ItemReader<List<Long>> dedupSimpleKeysReader(String tablename)
			throws Exception {
		JdbcPagingItemReader<List<Long>> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id_array");
		pqpf.setFromClause("FROM " + tablename);
		pqpf.setSortKey("id_array");
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

	@Bean(name = "dedupRecordsJob:writer")
	@StepScope
	public DedupRecordsWriter writer() {
		return new DedupRecordsWriter();
	}

	@Bean(name = "dedupRecordsJob:updateDedupRecordTasklet")
	@StepScope
	public Tasklet updateDedupRecordTasklet() {
		return new SqlCommandTasklet(updateDedupRecordSql);
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
