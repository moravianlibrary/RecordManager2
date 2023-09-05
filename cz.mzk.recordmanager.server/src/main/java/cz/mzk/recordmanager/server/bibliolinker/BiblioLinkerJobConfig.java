package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.bibliolinker.keys.BiblioLinkerJobParametersValidator;
import cz.mzk.recordmanager.server.dedup.KeyGeneratorForList;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.IntegerModuloPartitioner;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;
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

import javax.persistence.OptimisticLockException;
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
	private TaskExecutor taskExecutor;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	private static final String TMP_BL_TABLE_TITLE_AUTHOR = "tmp_bl_title_author";

	private static final String TMP_BL_TABLE_TITLE_AUTHOR_AUDIO = "tmp_bl_title_author_audio";

	private static final String TMP_BL_TABLE_TITLE_AUTHOR_VIDEO = "tmp_bl_title_author_video";

	private static final String TMP_BL_TABLE_TITLE_AUTHOR_MUSICAL_SCORE = "tmp_bl_title_author_ms";

	private static final String TMP_BL_TABLE_TITLE_LANG_PERIODICAL = "tmp_bl_title_lang_periodical";

	private static final String TMP_BL_TABLE_REST_DEDUP = "tmp_bl_rest_dedup";

	private static final String TMP_BL_TABLE_ORPHANED = "tmp_bl_orphaned";

	private static final String TMP_BLS_TABLE_AUTHOR_COMMON_TITLE = "tmp_bls_author_common_title";

	private static final String TMP_BLS_TABLE_AUTHOR_TITLE = "tmp_bls_author_title";

	private static final String TMP_BLS_TABLE_TOPIC_KEY = "tmp_bls_topic_key";

	private static final String TMP_BLS_TABLE_TOPIC_KEY_SPEC_DOCS = "tmp_bls_topic_key_spec_docs";

	private static final String TMP_BLS_TABLE_ENTITY = "tmp_bls_entity";

	private static final String TMP_BLS_TABLE_ISSN_SERIES = "tmp_bls_issn_series";

	private static final String TMP_BLS_TABLE_SERIES_PUBLISHER_LANG = "tmp_bls_series_publisher";

	private static final String TMP_BLS_TABLE_ENTITY_LANGUAGE_REST = "tmp_bls_entity_lang_rest";

	private static final String TMP_BLS_TABLE_TOPIC_KEY_LANGUAGE_REST = "tmp_bls_topic_key_lang_rest";

	private static final String TMP_BLS_TABLE_LIBRARIES = "tmp_bls_libraries";

	private static final String TMP_BLS_TABLE_REST = "tmp_bls_rest";

	private String initBiblioLinkerSql = ResourceUtils.asString("job/biblioLinkerJob/initBiblioLinker.sql");

	private String initBiblioLinkerSimilarSql = ResourceUtils.asString("job/biblioLinkerJob/initBiblioLinkerSimilar.sql");

	private String prepareBLTempTitleAuthorTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthor.sql");

	private String prepareBLTempTitleAuthorAudioTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthorAudio.sql");

	private String prepareBLTempTitleAuthorVideoTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthorVideo.sql");

	private String prepareBLTempTitleAuthorMusicalScoreTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthorMusicalScore.sql");

	private String prepareBLTempTitleLangPeriodicalTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleLangPeriodical.sql");

	private String prepareBLTempRestDedupTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempRestDedup.sql");

	private String prepareBLTempOrphanedTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempOrphaned.sql");

	private String prepareBLSimilarTempAuthorCommonTitleTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthorKeyCommonTitle.sql");

	private String prepareBLSimilarTempAuthorTitleTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthorTitle.sql");

	private String prepareBLSimilarTempAuthorTitleAudioMusicalScoreTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthorTitleAudioMusicalScore.sql");

	private String prepareBLSimilarTempTopicKeyTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempTopicKey.sql");

	private String prepareBLSimilarTempTopicKeySpecDocsTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempTopicKeySpecialDocuments.sql");

	private String prepareBLSimilarTempEntityTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempEntityLang.sql");

	private String prepareBLSimilarTempIssnSeriesTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempIssnSeries.sql");

	private String prepareBLSimilarTempSeriesPublisherTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempSeriesPublisher.sql");

	private String prepareBLSimilarTempLibrariesTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempLibraries.sql");

	private String prepareBLSimilarTempEntityLangRestTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempEntityLangRest.sql");

	private String prepareBLSimilarTempTopicKeyLangRestTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempTopicKeyLangRest.sql");

	private String prepareBLSimilarTempRestTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempRest.sql");

	private String blCleanupSql =
			ResourceUtils.asString("job/biblioLinkerJob/blCleanup.sql");

	private String blSimilarCleanupSql =
			ResourceUtils.asString("job/biblioLinkerJob/blSimilarCleanup.sql");

	private static final String blSimilarMaxCount =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempMaxCount.sql");

	private static final Integer INTEGER_OVERRIDEN_BY_EXPRESSION = null;

	private final int partitionThreads = 6;

	private static final int RETRY_LIMIT = 10000;

	@Bean
	public Job biblioLinkerJob(
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":initBLStep") Step initBLStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorStep") Step prepareBLTempTitleAuthorStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorPartitionedStep") Step blTempTitleAuthorStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorAudioStep") Step prepareBLTempTitleAuthorAudioStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorAudioPartitionedStep") Step blTempTitleAuthorAudioStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorVideoStep") Step prepareBLTempTitleAuthorVideoStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorVideoPartitionedStep") Step blTempTitleAuthorVideoStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorMusicalScoreStep") Step prepareBLTempTitleAuthorMusicalScoreStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorMusicalScorePartitionedStep") Step blTempTitleAuthorMusicalScoreStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleLangPeriodicalStep") Step prepareBLTempTitleLangPeriodicalStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleLangPeriodicalPartitionedStep") Step blTempTitleLangPeriodicalStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempRestDedupStep") Step prepareBLTempRestDedupStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempRestDedupPartitionedStep") Step blTempRestDedupStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempOrphanedStep") Step prepareBLTempOrphanedStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempOrphanedPartitionedStep") Step blTempOrphanedStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blCleanupStep") Step blCleanupStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER)
				.validator(new BiblioLinkerJobParametersValidator())
				.start(initBLStep)
				.next(prepareBLTempTitleAuthorStep)
				.next(blTempTitleAuthorStep)
				.next(prepareBLTempTitleAuthorAudioStep)
				.next(blTempTitleAuthorAudioStep)
				.next(prepareBLTempTitleAuthorVideoStep)
				.next(blTempTitleAuthorVideoStep)
				.next(prepareBLTempTitleAuthorMusicalScoreStep)
				.next(blTempTitleAuthorMusicalScoreStep)
				.next(prepareBLTempTitleLangPeriodicalStep)
				.next(blTempTitleLangPeriodicalStep)
				.next(prepareBLTempRestDedupStep)
				.next(blTempRestDedupStep)
				.next(prepareBLTempOrphanedStep)
				.next(blTempOrphanedStep)
				.next(blCleanupStep)
				.build();
	}

	/**
	 * Init biblio linker
	 */
	@Bean(name = "initBLTasklet")
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
	 * merge books, maps with same title and author
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthorTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthorTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorStep")
	public Step prepareBLTempTitleAuthorStep() {
		return steps.get("prepareBLTempTitleAuthorStep")
				.tasklet(prepareBLTempTitleAuthorTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorStep")
	public Step blTempTitleAuthorStep() throws Exception {
		return steps.get("blTempTitleAuthStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blTempTitleAuthorStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleBlWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorPartitionedStep")
	public Step blTempTitleAuthorPartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthorPartitionedStep")
				.partitioner("blTempTitleAuthorPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthorStep())
				.build();
	}

	@Bean(name = "blTitleAuthor:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthorStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTHOR, "dedup_record_id", modulo);
	}

	/**
	 * merge audio records with same title and author
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorAudioTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthorAudioTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthorAudioTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorAudioStep")
	public Step prepareBLTempTitleAuthorAudioStep() {
		return steps.get("prepareBLTempTitleAuthorAudioStep")
				.tasklet(prepareBLTempTitleAuthorAudioTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorAudioStep")
	public Step blTempTitleAuthorAudioStep() throws Exception {
		return steps.get("blTempTitleAuthorAudioStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blTempTitleAuthorAudioStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleBlWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorAudioPartitionedStep")
	public Step blTempTitleAuthorAudioPartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthorAudioPartitionedStep")
				.partitioner("blTempTitleAuthorAudioPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthorAudioStep())
				.build();
	}

	@Bean(name = "blTitleAuthorAudio:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthorAudioStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTHOR_AUDIO, "dedup_record_id", modulo);
	}

	/**
	 * merge video records with same title and author
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorVideoTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthorVideoTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthorVideoTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorVideoStep")
	public Step prepareBLTempTitleAuthorVideoStep() {
		return steps.get("prepareBLTempTitleAuthorVideoStep")
				.tasklet(prepareBLTempTitleAuthorVideoTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorVideoStep")
	public Step blTempTitleAuthorVideoStep() throws Exception {
		return steps.get("blTempTitleAuthorVideoStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blTempTitleAuthorVideoStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleBlWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorVideoPartitionedStep")
	public Step blTempTitleAuthorVideoPartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthorVideoPartitionedStep")
				.partitioner("blTempTitleAuthorVideoPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthorVideoStep())
				.build();
	}

	@Bean(name = "blTitleAuthorVideo:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthorVideoStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTHOR_VIDEO, "dedup_record_id", modulo);
	}

	/**
	 * merge musical score records with same title and author
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorMusicalScoreTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthorMusicalScoreTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthorMusicalScoreTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthorMusicalScoreStep")
	public Step prepareBLTempTitleAuthorMusicalScoreStep() {
		return steps.get("prepareBLTempTitleAuthorMusicalScoreStep")
				.tasklet(prepareBLTempTitleAuthorMusicalScoreTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorMusicalScoreStep")
	public Step blTempTitleAuthorMusicalScoreStep() throws Exception {
		return steps.get("blTempTitleAuthorMusicalScoreStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blTempTitleAuthorMusicalScoreStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleBlWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorMusicalScorePartitionedStep")
	public Step blTempTitleAuthorMusicalScorePartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthorMusicalScorePartitionedStep")
				.partitioner("blTempTitleAuthorMusicalScorePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthorMusicalScoreStep())
				.build();
	}

	@Bean(name = "blTitleAuthorMusicalScore:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthorMusicalScoreStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTHOR_MUSICAL_SCORE, "dedup_record_id", modulo);
	}

	/**
	 * merge periodical records with same title and language
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleLangPeriodicalTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleLangPeriodicalTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleLangPeriodicalTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleLangPeriodicalStep")
	public Step prepareBLTempTitleLangPeriodicalStep() {
		return steps.get("prepareBLTempTitleLangPeriodicalStep")
				.tasklet(prepareBLTempTitleLangPeriodicalTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleLangPeriodicalStep")
	public Step blTempTitleLangPeriodicalStep() throws Exception {
		return steps.get("blTempTitleLangPeriodicalStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(2 * RETRY_LIMIT)
				.reader(blTempTitleLangPeriodicalStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleBlWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleLangPeriodicalPartitionedStep")
	public Step blTempTitleLangPeriodicalPartitionedStep() throws Exception {
		return steps.get("blTempTitleLangPeriodicalPartitionedStep")
				.partitioner("blTempTitleLangPeriodicalPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleLangPeriodicalStep())
				.build();
	}

	@Bean(name = "blTitleLangPeriodical:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleLangPeriodicalStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_LANG_PERIODICAL, "dedup_record_id", modulo);
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
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blTempRestDedupStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleBlWriter())
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
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blTempOrphanedStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleBlWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempOrphanedPartitionedStep")
	public Step blTempOrphanedPartitionedStep() throws Exception {
		return steps.get("blTempOrphanedPartitionedStep")
				.partitioner("blTempOrphanedPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempOrphanedStep())
				.build();
	}

	@Bean(name = "blOrphaned:reader")
	@StepScope
	public ItemReader<List<Long>> blTempOrphanedStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_ORPHANED, "dedup_record_id", modulo);
	}

	/**
	 * Cleanup
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blCleanupStep")
	public Step blCleanupStep() {
		return steps.get("blCleanupStep")
				.tasklet(blcleanupTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "blCleanupStep:blcleanupTasklet")
	@StepScope
	public Tasklet blcleanupTasklet() {
		return new SqlCommandTasklet(blCleanupSql);
	}

	/**
	 * biblioLinkerSimilarJob
	 */
	@Bean
	public Job biblioLinkerSimilarJob(
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":initBLSStep") Step initBLSStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorCommonTitleStep") Step prepareBLSimilarTempAuthorCommonTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorCommonTitlePartitionedStep") Step blSimilarTempAuthorCommonTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorTitleStep") Step prepareBLSimilarTempAuthorTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorTitlePartitionedStep") Step blSimilarTempAuthorTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeyStep") Step prepareBLSimilarTempTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeyPartitionedStep") Step blSimilarTempTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeySpecDocsStep") Step prepareBLSimilarTempTopicKeySpecDocsStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeySpecDocsPartitionedStep") Step blSimilarTempTopicKeySpecDocsStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityStep") Step prepareBLSimilarTempEntityStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityPartitionedStep") Step blSimilarTempEntityStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempIssnSeriesStep") Step prepareBLSimilarTempIssnSeriesStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempIssnSeriesPartitionedStep") Step blSimilarTempIssnSeriesStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSeriesPublisherStep") Step prepareBLSimilarTempSeriesPublisherStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSeriesPublisherPartitionedStep") Step blSimilarTempSeriesPublisherStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempLibrariesStep") Step prepareBLSimilarTempLibrariesStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempLibrariesPartitionedStep") Step blSimilarTempLibrariesStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityLangRestStep") Step prepareBLSimilarTempEntityLangRestStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityLangRestPartitionedStep") Step blSimilarTempEntityLangRestStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeyLangRestStep") Step prepareBLSimilarTempTopicKeyLangRestStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeyLangRestPartitionedStep") Step blSimilarTempTopicKeyLangRestStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempRestStep") Step prepareBLSimilarTempRestStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempRestPartitionedStep") Step blSimilarTempRestStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarCleanupStep") Step blSimilarCleanupStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarMaxCountStep") Step prepareBLSimilarMaxCountStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR)
				.validator(new BiblioLinkerJobParametersValidator())
				.start(initBLSStep)
				.next(prepareBLSimilarTempAuthorCommonTitleStep)
				.next(blSimilarTempAuthorCommonTitleStep)
				.next(prepareBLSimilarTempAuthorTitleStep)
				.next(blSimilarTempAuthorTitleStep)
				.next(prepareBLSimilarTempIssnSeriesStep)
				.next(blSimilarTempIssnSeriesStep)
				.next(prepareBLSimilarTempSeriesPublisherStep)
				.next(blSimilarTempSeriesPublisherStep)
				.next(prepareBLSimilarTempEntityStep)
				.next(blSimilarTempEntityStep)
//				.next(prepareBLSimilarTempTopicKeySpecDocsStep)
//				.next(blSimilarTempTopicKeySpecDocsStep)
				.next(prepareBLSimilarTempTopicKeyStep)
				.next(blSimilarTempTopicKeyStep)
				.next(prepareBLSimilarTempLibrariesStep)
				.next(blSimilarTempLibrariesStep)
				.next(prepareBLSimilarMaxCountStep)
				.next(prepareBLSimilarTempEntityLangRestStep)
				.next(blSimilarTempEntityLangRestStep)
				.next(prepareBLSimilarMaxCountStep)
				.next(prepareBLSimilarTempTopicKeyLangRestStep)
				.next(blSimilarTempTopicKeyLangRestStep)
				.next(prepareBLSimilarMaxCountStep)
				.next(prepareBLSimilarTempRestStep)
				.next(blSimilarTempRestStep)
				.next(blSimilarCleanupStep)
				.build();
	}

	/**
	 * Init biblio linker similar job
	 */
	@Bean(name = "initBLSTasklet")
	@StepScope
	public Tasklet initBLSTasklet() {
		return new SqlCommandTasklet(initBiblioLinkerSimilarSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":initBLSStep")
	public Step initBLSStep() {
		return steps.get("initBLSTasklet")
				.tasklet(initBLSTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	/**
	 * same autor, common title, language, format
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorCommonTitleTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthorCommonTitleTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthorCommonTitleTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorCommonTitleStep")
	public Step prepareBLSimilarTempAuthorCommonTitleStep() {
		return steps.get("prepareBLSimilarTempAuthorCommonTitleStep")
				.tasklet(prepareBLSimilarTempAuthorCommonTitleTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorCommonTitleStep")
	public Step blSimilarTempAuthorCommonTitleStep() throws Exception {
		return steps.get("blSimilarTempAuthorCommonTitleStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempAuthorCommonTitleStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthorCommonTitleStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorCommonTitlePartitionedStep")
	public Step blSimilarTempAuthorCommonTitlePartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthorCommonTitlePartitionedStep")
				.partitioner("blSimilarTempAuthorCommonTitlePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthorCommonTitleStep())
				.build();
	}

	@Bean(name = "blSimilarAuthorCommonTitle:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthorCommonTitleStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTHOR_COMMON_TITLE, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthorCommonTitle:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthorCommonTitleStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTHOR_COMMON_TITLE);
	}

	/**
	 * same author, title for books, audio, video, musical score
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorTitleTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthorTitleTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthorTitleTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorTitleStep")
	public Step prepareBLSimilarTempAuthorTitleStep() {
		return steps.get("prepareBLSimilarTempAuthorTitleStep")
				.tasklet(prepareBLSimilarTempAuthorTitleTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorTitleStep")
	public Step blSimilarTempAuthorTitleStep() throws Exception {
		return steps.get("blSimilarTempAuthorTitleStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempAuthorTitleStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthorTitleStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorTitlePartitionedStep")
	public Step blSimilarTempAuthorTitlePartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthorTitlePartitionedStep")
				.partitioner("blSimilarTempAuthorTitlePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthorTitleStep())
				.build();
	}

	@Bean(name = "blSimilarAuthorTitle:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthorTitleStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTHOR_TITLE, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthorTitle:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthorTitleStepProsessor() {
		return new BiblioLinkerSimilarAuthorTitleStepProcessor(BiblioLinkerSimilarType.AUTHOR_TITLE);
	}

	/**
	 * same author, title, for audio, musical score
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorTitleAudioMusicalScoreTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthorTitleAudioMusicalScoreTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthorTitleAudioMusicalScoreTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorTitleAudioMusicalScoreStep")
	public Step prepareBLSimilarTempAuthorTitleAudioMusicalScoreStep() {
		return steps.get("prepareBLSimilarTempAuthorTitleAudioMusicalScoreStep")
				.tasklet(prepareBLSimilarTempAuthorTitleAudioMusicalScoreTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorTitleAudioMusicalScoreStep")
	public Step blSimilarTempAuthorTitleAudioMusicalScoreStep() throws Exception {
		return steps.get("blSimilarTempAuthorTitleAudioMusicalScoreStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempAuthorTitleAudioMusicalScoreStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthorTitleAudioMusicalScoreStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorTitleAudioMusicalScorePartitionedStep")
	public Step blSimilarTempAuthorTitleAudioMusicalScorePartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthorTitleAudioMusicalScorePartitionedStep")
				.partitioner("blSimilarTempAuthorTitleAudioMusicalScorePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthorTitleAudioMusicalScoreStep())
				.build();
	}

	@Bean(name = "blSimilarAuthorTitleAudioMusicalScore:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthorTitleAudioMusicalScoreStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTHOR_TITLE, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthorTitleAudioMusicalScore:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthorTitleAudioMusicalScoreStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTHOR_TITLE);
	}

	/**
	 * same topic_key, language for periodicals
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeyTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempTopicKeyTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempTopicKeyTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeyStep")
	public Step prepareBLSimilarTempTopicKeyStep() {
		return steps.get("prepareBLSimilarTempTopicKeyStep")
				.tasklet(prepareBLSimilarTempTopicKeyTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeyStep")
	public Step blSimilarTempTopicKeyStep() throws Exception {
		return steps.get("blSimilarTempTopicKeyStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(2 * RETRY_LIMIT)
				.reader(blSimilarTempTopicKeyStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarTopicKeyStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeyPartitionedStep")
	public Step blSimilarTempTopicKeyPartitionedStep() throws Exception {
		return steps.get("blSimilarTempTopicKeyPartitionedStep")
				.partitioner("blSimilarTempTopicKeyPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempTopicKeyStep())
				.build();
	}

	@Bean(name = "blSimilarTopicKey:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempTopicKeyStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_TOPIC_KEY, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarTopicKey:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarTopicKeyStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.TOPIC_KEY);
	}

	/**
	 * same topic_key, language for periodicals - patents, authorities
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeySpecDocsTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempTopicKeySpecDocsTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempTopicKeySpecDocsTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeySpecDocsStep")
	public Step prepareBLSimilarTempTopicKeySpecDocsStep() {
		return steps.get("prepareBLSimilarTempTopicKeySpecDocsStep")
				.tasklet(prepareBLSimilarTempTopicKeySpecDocsTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeySpecDocsStep")
	public Step blSimilarTempTopicKeySpecDocsStep() throws Exception {
		return steps.get("blSimilarTempTopicKeySpecDocsStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(2 * RETRY_LIMIT)
				.reader(blSimilarTempTopicKeySpecDocsStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarTopicKeySpecDocsStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeySpecDocsPartitionedStep")
	public Step blSimilarTempTopicKeySpecDocsPartitionedStep() throws Exception {
		return steps.get("blSimilarTempTopicKeySpecDocsPartitionedStep")
				.partitioner("blSimilarTempTopicKeySpecDocsPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempTopicKeySpecDocsStep())
				.build();
	}

	@Bean(name = "blSimilarTopicKeySpecDocs:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempTopicKeySpecDocsStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_TOPIC_KEY_SPEC_DOCS, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarTopicKeySpecDocs:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarTopicKeySpecDocsStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.TOPIC_KEY);
	}

	/**
	 * same entity
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempEntityTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempEntityTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityStep")
	public Step prepareBLSimilarTempEntityStep() {
		return steps.get("prepareBLSimilarTempEntityStep")
				.tasklet(prepareBLSimilarTempEntityTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityStep")
	public Step blSimilarTempEntityStep() throws Exception {
		return steps.get("blSimilarTempEntityStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempEntityStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarEntityStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityPartitionedStep")
	public Step blSimilarTempEntityPartitionedStep() throws Exception {
		return steps.get("blSimilarTempEntityPartitionedStep")
				.partitioner("blSimilarTempEntityPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempEntityStep())
				.build();
	}

	@Bean(name = "blSimilarEntity:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempEntityStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_ENTITY, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarEntity:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarEntityStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.ENTITY);
	}

	/**
	 * same issn_series for books
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempIssnSeriesTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempIssnSeriesTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempIssnSeriesTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempIssnSeriesStep")
	public Step prepareBLSimilarTempIssnSeriesStep() {
		return steps.get("prepareBLSimilarTempIssnSeriesStep")
				.tasklet(prepareBLSimilarTempIssnSeriesTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempIssnSeriesStep")
	public Step blSimilarTempIssnSeriesStep() throws Exception {
		return steps.get("blSimilarTempIssnSeriesStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempIssnSeriesStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarIssnSeriesStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempIssnSeriesPartitionedStep")
	public Step blSimilarTempIssnSeriesPartitionedStep() throws Exception {
		return steps.get("blSimilarTempIssnSeriesPartitionedStep")
				.partitioner("blSimilarTempIssnSeriesPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempIssnSeriesStep())
				.build();
	}

	@Bean(name = "blSimilarIssnSeries:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempIssnSeriesStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_ISSN_SERIES, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarIssnSeries:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarIssnSeriesStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.ISSN_SERIES);
	}

	/**
	 * same series, publisher, lang
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSeriesPublisherTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempSeriesPublisherTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempSeriesPublisherTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSeriesPublisherStep")
	public Step prepareBLSimilarTempSeriesPublisherStep() {
		return steps.get("prepareBLSimilarTempSeriesPublisherStep")
				.tasklet(prepareBLSimilarTempSeriesPublisherTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSeriesPublisherStep")
	public Step blSimilarTempSeriesPublisherStep() throws Exception {
		return steps.get("blSimilarTempSeriesPublisherStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempSeriesPublisherStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarSeriesPublisherStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSeriesPublisherPartitionedStep")
	public Step blSimilarTempSeriesPublisherPartitionedStep() throws Exception {
		return steps.get("blSimilarTempSeriesPublisherPartitionedStep")
				.partitioner("blSimilarTempSeriesPublisherPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempSeriesPublisherStep())
				.build();
	}

	@Bean(name = "blSimilarSeriesPublisher:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempSeriesPublisherStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_SERIES_PUBLISHER_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarSeriesPublisher:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarSeriesPublisherStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.SERIES_PUBLISHE);
	}

	/**
	 * libraries
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempLibrariesTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempLibrariesTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempLibrariesTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempLibrariesStep")
	public Step prepareBLSimilarTempLibrariesStep() {
		return steps.get("prepareBLSimilarTempLibrariesStep")
				.tasklet(prepareBLSimilarTempLibrariesTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempLibrariesStep")
	public Step blSimilarTempLibrariesStep() throws Exception {
		return steps.get("blSimilarTempLibrariesStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempLibrariesStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarLibrariesStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempLibrariesPartitionedStep")
	public Step blSimilarTempLibrariesPartitionedStep() throws Exception {
		return steps.get("blSimilarTempLibrariesPartitionedStep")
				.partitioner("blSimilarTempLibrariesPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempLibrariesStep())
				.build();
	}

	@Bean(name = "blSimilarLibraries:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempLibrariesStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_LIBRARIES, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarLibraries:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarLibrariesStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.ENTITY_LIBRARIES);
	}


	/**
	 * same entity, language, only records without similar
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityLangRestTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempEntityLangRestTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempEntityLangRestTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityLangRestStep")
	public Step prepareBLSimilarTempEntityLangRestStep() {
		return steps.get("prepareBLSimilarTempEntityLangRestStep")
				.tasklet(prepareBLSimilarTempEntityLangRestTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityLangRestStep")
	public Step blSimilarTempEntityLangRestStep() throws Exception {
		return steps.get("blSimilarTempEntityLangRestStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(2 * RETRY_LIMIT)
				.reader(blSimilarTempEntityLangRestStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarEntityLangRestStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityLangRestPartitionedStep")
	public Step blSimilarTempEntityLangRestPartitionedStep() throws Exception {
		return steps.get("blSimilarTempEntityLangRestPartitionedStep")
				.partitioner("blSimilarTempEntityLangRestPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempEntityLangRestStep())
				.build();
	}

	@Bean(name = "blSimilarEntityLangRest:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempEntityLangRestStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_ENTITY_LANGUAGE_REST, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarEntityLangRest:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarEntityLangRestStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.ENTITY_LANGUAGE, 5);
	}

	/**
	 * same topic key, language, only records without similar
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeyLangRestTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempTopicKeyLangRestTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempTopicKeyLangRestTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeyLangRestStep")
	public Step prepareBLSimilarTempTopicKeyLangRestStep() {
		return steps.get("prepareBLSimilarTempTopicKeyLangRestStep")
				.tasklet(prepareBLSimilarTempTopicKeyLangRestTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeyLangRestStep")
	public Step blSimilarTempTopicKeyLangRestStep() throws Exception {
		return steps.get("blSimilarTempTopicKeyLangRestStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(2 * RETRY_LIMIT)
				.reader(blSimilarTempTopicKeyLangRestStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarTopicKeyLangRestStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeyLangRestPartitionedStep")
	public Step blSimilarTempTopicKeyLangRestPartitionedStep() throws Exception {
		return steps.get("blSimilarTempTopicKeyLangRestPartitionedStep")
				.partitioner("blSimilarTempTopicKeyLangRestPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempTopicKeyLangRestStep())
				.build();
	}

	@Bean(name = "blSimilarTopicKeyLangRest:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempTopicKeyLangRestStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_TOPIC_KEY_LANGUAGE_REST, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarTopicKeyLangRest:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarTopicKeyLangRestStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.TOPICKEY_LANGUAGE, 5);
	}

	/**
	 * only records without similar
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempRestTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempRestTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempRestTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempRestStep")
	public Step prepareBLSimilarTempRestStep() {
		return steps.get("prepareBLSimilarTempRestStep")
				.tasklet(prepareBLSimilarTempRestTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempRestStep")
	public Step blSimilarTempRestStep() throws Exception {
		return steps.get("blSimilarTempRestStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(RETRY_LIMIT)
				.reader(blSimilarTempRestStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarRestStepProsessor())
				.writer(blSimpleSimilarWriter(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempRestPartitionedStep")
	public Step blSimilarTempRestPartitionedStep() throws Exception {
		return steps.get("blSimilarTempRestPartitionedStep")
				.partitioner("blSimilarTempRestPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempRestStep())
				.build();
	}

	@Bean(name = "blSimilarRest:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempRestStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_REST, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarRest:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarRestStepProsessor() {
		return new BiblioLinkerSimilarRestStepProcessor(BiblioLinkerSimilarType.REST);
	}

	/**
	 * Cleanup BLS
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarCleanupStep")
	public Step blSimilarCleanupStep() {
		return steps.get("blSimilarCleanupStep")
				.tasklet(blSimilarcleanupTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "blSimilarCleanupStep:blSimilarcleanupTasklet")
	@StepScope
	public Tasklet blSimilarcleanupTasklet() {
		return new SqlCommandTasklet(blSimilarCleanupSql);
	}

	/**
	 * biblio linker similar max count step
	 */
	@Bean(name = "blsMaxCountTasklet")
	@StepScope
	public Tasklet blsMaxCountTasklet() {
		return new SqlCommandTasklet(blSimilarMaxCount);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarMaxCountStep")
	public Step prepareBLSimilarMaxCountStep() {
		return steps.get("prepareBLSimilarMaxCountStep")
				.tasklet(blsMaxCountTasklet())
				.listener(new StepProgressListener())
				.build();
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

	@Bean(name = "blSimpleBl:writer")
	@StepScope
	public ItemWriter<List<HarvestedRecord>> blSimpleBlWriter() throws Exception {
		return new BiblioLinkerSimpleKeysStepWriter(0);
	}

	@Bean(name = "blSimpleSimilar:writer")
	@StepScope
	public ItemWriter<List<HarvestedRecord>> blSimpleSimilarWriter(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_UPDATE_TIMESTAMP + "]}") Integer updateTimestamp
	) throws Exception {
		return new BiblioLinkerSimpleKeysStepWriter(updateTimestamp);
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
