package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.dedup.DedupRecordsJobParametersValidator;
import cz.mzk.recordmanager.server.dedup.KeyGeneratorForList;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.IntegerModuloPartitioner;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.hibernate.SessionFactory;
import org.hibernate.exception.LockAcquisitionException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BiblioLinkerJobConfig {

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

	private static final String TMP_BL_TABLE_TITLE_AUTH = "tmp_bl_title_auth";

	private static final String TMP_BL_TABLE_REST_DEDUP = "tmp_bl_rest_dedup";

	private static final String TMP_BL_TABLE_ORPHANED = "tmp_bl_orphaned";

	private static final String TMP_BLS_TABLE_AUTH = "tmp_bls_auth";

	private static final String TMP_BLS_TABLE_CONSPECTUS = "tmp_bls_conspectus";

	private static final String TMP_BLS_TABLE_AUTH_CONSPECTUS = "tmp_bls_auth_conspectus";

	private String initBiblioLinkerSql = ResourceUtils.asString("job/biblioLinkerJob/initBiblioLinker.sql");

	private String prepareBLTempTitleAuthTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuth.sql");

	private String prepareBLTempRestDedupTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempRestDedup.sql");

	private String prepareBLTempOrphanedTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempOrphaned.sql");

	private String prepareBLSimilarTempAuthConspectusTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthConspectus.sql");

	private String prepareBLSimilarTempConspectusTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempConspectus.sql");

	private String prepareBLSimilarTempAuthTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuth.sql");

	private static final Integer INTEGER_OVERRIDEN_BY_EXPRESSION = null;

	private int partitionThreads = 4;

	@Bean
	public Job biblioLinkerJob(
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":initBLStep") Step initBLStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthStep") Step prepareBLTempTitleAuthStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthPartitionedStep") Step blTempTitleAuthStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempRestDedupStep") Step prepareBLTempRestDedupStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempRestDedupPartitionedStep") Step blTempRestDedupStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempOrphanedStep") Step prepareBLTempOrphanedStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempOrphanedStep") Step blTempOrphanedStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER)
				.validator(new DedupRecordsJobParametersValidator())
				.start(initBLStep)
				.next(prepareBLTempTitleAuthStep)
				.next(blTempTitleAuthStep)
				.next(prepareBLTempRestDedupStep)
				.next(blTempRestDedupStep)
				.next(prepareBLTempOrphanedStep)
				.next(blTempOrphanedStep)
				.build();
	}

	/**
	 * Init biblio linker
	 */
	@Bean(name = "initTasklet")
	@StepScope
	public Tasklet initBLTasklet() {
		return new SqlCommandTasklet(initBiblioLinkerSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":initBLStep")
	public Step initBLStep() {
		return steps.get("initBLTasklet")
				.tasklet(initBLTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	/**
	 * merge records with same title and authority
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthStep")
	public Step prepareBLTempTitleAuthStep() {
		return steps.get("prepareBLTempTitleAuthStep")
				.tasklet(prepareBLTempTitleAuthTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthStep")
	public Step blTempTitleAuthStep() throws Exception {
		return steps.get("blTempTitleAuthStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleAuthStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthPartitionedStep")
	public Step blTempTitleAuthPartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthPartitionedStep")
				.partitioner("blTempTitleAuthPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthStep())
				.build();
	}

	@Bean(name = "blTitleAuth:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTH, "dedup_record_id", modulo);
	}

	/**
	 * more records with same dedup_record
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempRestDedupTasklet")
	@StepScope
	public Tasklet prepareBLTempRestDedupTasklet() {
		return new SqlCommandTasklet(prepareBLTempRestDedupTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempRestDedupStep")
	public Step prepareBLTempRestDedupStep() {
		return steps.get("prepareBLTempRestDedupStep")
				.tasklet(prepareBLTempRestDedupTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempRestDedupStep")
	public Step blTempRestDedupStep() throws Exception {
		return steps.get("blTempRestDedupStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempRestDedupStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempRestDedupPartitionedStep")
	public Step blTempRestDedupPartitionedStep() throws Exception {
		return steps.get("blTempRestDedupPartitionedStep")
				.partitioner("blTempRestDedupPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempRestDedupStep())
				.build();
	}

	@Bean(name = "blRestDedup:reader")
	@StepScope
	public ItemReader<List<Long>> blTempRestDedupStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_REST_DEDUP, "dedup_record_id", modulo);
	}

	/**
	 * orphaned records
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempOrphanedTasklet")
	@StepScope
	public Tasklet prepareBLTempOrphanedTasklet() {
		return new SqlCommandTasklet(prepareBLTempOrphanedTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempOrphanedStep")
	public Step prepareBLTempOrphanedStep() {
		return steps.get("prepareBLTempOrphanedStep")
				.tasklet(prepareBLTempOrphanedTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempOrphanedStep")
	public Step blTempOrphanedStep() throws Exception {
		return steps.get("blTempOrphanedStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.reader(blTempOrphanedStepReader())
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = "blOrphaned:reader")
	@StepScope
	public ItemReader<List<Long>> blTempOrphanedStepReader() throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_ORPHANED, "dedup_record_id", INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	/**
	 * biblioLinkerSimilarJob
	 */
	@Bean
	public Job biblioLinkerSimilarJob(
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthConspectusStep") Step prepareBLSimilarTempAuthConspectusStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthConspectusPartitionedStep") Step blSimilarTempAuthConspectusStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempConspectusStep") Step prepareBLSimilarTempConspectusStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempConspectusPartitionedStep") Step blSimilarTempConspectustep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthStep") Step prepareBLSimilarTempAuthStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthPartitionedStep") Step blSimilarTempAuthStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR)
				.validator(new DedupRecordsJobParametersValidator())
				.start(prepareBLSimilarTempAuthConspectusStep)
				.next(blSimilarTempAuthConspectusStep)
				.next(prepareBLSimilarTempConspectusStep)
				.next(blSimilarTempConspectustep)
				.next(prepareBLSimilarTempAuthStep)
				.next(blSimilarTempAuthStep)
				.build();
	}

	/**
	 * same autority, conspectus
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthConspectusTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthConspectusTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthConspectusTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthConspectusStep")
	public Step prepareBLSimilarTempAuthConspectusStep() {
		return steps.get("prepareBLSimilarTempAuthConspectusStep")
				.tasklet(prepareBLSimilarTempAuthConspectusTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthConspectusStep")
	public Step blSimilarTempAuthConspectusStep() throws Exception {
		return steps.get("blSimilarTempAuthConspectusStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthConspectusStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthConspectusStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthConspectusPartitionedStep")
	public Step blSimilarTempAuthConspectusPartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthConspectusPartitionedStep")
				.partitioner("blSimilarTempAuthConspectusPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthConspectusStep())
				.build();
	}

	@Bean(name = "blSimilarTitleAuth:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthConspectusStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH_CONSPECTUS, "local_record_id", modulo);
	}

	@Bean(name = "blSimilarAuthConspectus:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthConspectusStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTH_CONSPECTUS);
	}

	/**
	 * same conspectus
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempConspectusTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempConspectusTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempConspectusTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempConspectusStep")
	public Step prepareBLSimilarTempConspectusStep() {
		return steps.get("prepareBLSimilarTempConspectusStep")
				.tasklet(prepareBLSimilarTempConspectusTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempConspectusStep")
	public Step blSimilarTempConspectusStep() throws Exception {
		return steps.get("blSimilarTempConspectusStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempConspectusStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarConspectusStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempConspectusPartitionedStep")
	public Step blSimilarTempConspectusPartitionedStep() throws Exception {
		return steps.get("blSimilarTempConspectusPartitionedStep")
				.partitioner("blSimilarTempConspectusPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempConspectusStep())
				.build();
	}

	@Bean(name = "blSimilarConspectus:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempConspectusStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_CONSPECTUS, "local_record_id", modulo);
	}

	@Bean(name = "blSimilarConspectus:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarConspectusStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.CONSPECTUS);
	}

	/**
	 * same autority
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthStep")
	public Step prepareBLSimilarTempAuthStep() {
		return steps.get("prepareBLSimilarTempAuthStep")
				.tasklet(prepareBLSimilarTempAuthTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthStep")
	public Step blSimilarTempAuthStep() throws Exception {
		return steps.get("blSimilarTempAuthStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthPartitionedStep")
	public Step blSimilarTempAuthPartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthPartitionedStep")
				.partitioner("blSimilarTempAuthPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthStep())
				.build();
	}

	@Bean(name = "blSimilarAuth:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH, "local_record_id", modulo);
	}

	@Bean(name = "blSimilarAuth:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTH);
	}

	/**
	 * Generic components
	 */
	private ItemReader<List<Long>> blSimpleKeysReader(
			String tablename, String column,
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		JdbcPagingItemReader<List<Long>> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT row_id," + column + " ids");
		pqpf.setFromClause("FROM " + tablename);
		if (modulo != null) {
			pqpf.setWhereClause("WHERE row_id % :threads = :modulo");
		}
		pqpf.setSortKey("row_id");
		reader.setRowMapper(new ArrayLongMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		if (modulo != null) {
			Map<String, Object> parameterValues = new HashMap<>();
			parameterValues.put("threads", this.partitionThreads);
			parameterValues.put("modulo", modulo);
			reader.setParameterValues(parameterValues);
		}
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = "blSimpleKeys:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimpleKeysStepProsessor() {
		return new BiblioLinkerSimpleKeysStepProcessor();
	}

	@Bean(name = "blSimpleKeys:writer")
	@StepScope
	public ItemWriter<List<HarvestedRecord>> blSimpleKeysStepWriter()
			throws Exception {
		return new BiblioLinkerSimpleKeysStepWriter();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blSimpleKeysPartioner")
	@StepScope
	public IntegerModuloPartitioner partioner() {
		return new IntegerModuloPartitioner();
	}

	public class ArrayLongMapper implements RowMapper<List<Long>> {

		@Override
		public List<Long> mapRow(ResultSet arg0, int arg1) throws SQLException {
			List<Long> hrs = new ArrayList<>();
			String ids = arg0.getString("ids");
			for (String idStr : ids.split(",")) {
				Long hrId = Long.valueOf(idStr);
				hrs.add(hrId);
			}
			return hrs;
		}
	}
}
