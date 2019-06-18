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
	private TaskExecutor taskExecutor;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	private static final String TMP_BL_TABLE_TITLE_AUTH = "tmp_bl_title_auth";

	private static final String TMP_BL_TABLE_TITLE_AUTH_AUDIO = "tmp_bl_title_auth_audio";

	private static final String TMP_BL_TABLE_TITLE_AUTH_VIDEO = "tmp_bl_title_auth_video";

	private static final String TMP_BL_TABLE_TITLE_AUTH_MUSICAL_SCORE = "tmp_bl_title_auth_ms";

	private static final String TMP_BL_TABLE_TITLE_AUTHOR = "tmp_bl_title_author";

	private static final String TMP_BL_TABLE_TITLE_AUTHOR_AUDIO = "tmp_bl_title_author_audio";

	private static final String TMP_BL_TABLE_TITLE_AUTHOR_VIDEO = "tmp_bl_title_author_video";

	private static final String TMP_BL_TABLE_TITLE_AUTHOR_MUSICAL_SCORE = "tmp_bl_title_author_ms";

	private static final String TMP_BL_TABLE_TITLE_LANG_PERIODICAL = "tmp_bl_title_lang_periodical";

	private static final String TMP_BL_TABLE_REST_DEDUP = "tmp_bl_rest_dedup";

	private static final String TMP_BL_TABLE_ORPHANED = "tmp_bl_orphaned";

	private static final String TMP_BLS_TABLE_AUTH = "tmp_bls_auth";

	private static final String TMP_BLS_TABLE_CONSPECTUS = "tmp_bls_conspectus";

	private static final String TMP_BLS_TABLE_AUTH_CONSPECTUS = "tmp_bls_auth_conspectus";

	private String initBiblioLinkerSql = ResourceUtils.asString("job/biblioLinkerJob/initBiblioLinker.sql");

	private String initBiblioLinkerSimilarSql = ResourceUtils.asString("job/biblioLinkerJob/initBiblioLinkerSimilar.sql");

	private String prepareBLTempTitleAuthTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuth.sql");

	private String prepareBLTempTitleAuthAudioTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthAudio.sql");

	private String prepareBLTempTitleAuthVideoTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthVideo.sql");

	private String prepareBLTempTitleAuthMusicalScoreTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthMusicalScore.sql");

	private String prepareBLTempTitleAuthorTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthor.sql");

	private String prepareBLTempTitleAuthorAudioTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthorAudio.sql");

	private String prepareBLTempTitleAuthorVideoTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthorVideo.sql");

	private String prepareBLTempTitleAuthorMusicalScoreTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuthorMusicalScore.sql");

	private String prepareBLTempTitleLangPeriodicalTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleLangPeriodical.sql");

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
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthAudioStep") Step prepareBLTempTitleAuthAudioStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthAudioPartitionedStep") Step blTempTitleAuthAudioStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthVideoStep") Step prepareBLTempTitleAuthVideoStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthVideoPartitionedStep") Step blTempTitleAuthVideoStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthMusicalScoreStep") Step prepareBLTempTitleAuthMusicalScoreStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthMusicalScorePartitionedStep") Step blTempTitleAuthMusicalScoreStep,
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
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempOrphanedPartitionedStep") Step blTempOrphanedStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER)
				.validator(new DedupRecordsJobParametersValidator())
				.start(initBLStep)
				.next(prepareBLTempTitleAuthStep)
				.next(blTempTitleAuthStep)
				.next(prepareBLTempTitleAuthAudioStep)
				.next(blTempTitleAuthAudioStep)
				.next(prepareBLTempTitleAuthVideoStep)
				.next(blTempTitleAuthVideoStep)
				.next(prepareBLTempTitleAuthMusicalScoreStep)
				.next(blTempTitleAuthMusicalScoreStep)
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
	 * merge audio records with same title and authority
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthAudioTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthAudioTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthAudioTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthAudioStep")
	public Step prepareBLTempTitleAuthAudioStep() {
		return steps.get("prepareBLTempTitleAuthAudioStep")
				.tasklet(prepareBLTempTitleAuthAudioTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthAudioStep")
	public Step blTempTitleAuthAudioStep() throws Exception {
		return steps.get("blTempTitleAuthAudioStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleAuthAudioStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthAudioPartitionedStep")
	public Step blTempTitleAuthAudioPartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthAudioPartitionedStep")
				.partitioner("blTempTitleAuthAudioPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthAudioStep())
				.build();
	}

	@Bean(name = "blTitleAuthAudio:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthAudioStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTH_AUDIO, "dedup_record_id", modulo);
	}

	/**
	 * merge video records with same title and authority
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthVideoTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthVideoTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthVideoTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthVideoStep")
	public Step prepareBLTempTitleAuthVideoStep() {
		return steps.get("prepareBLTempTitleAuthVideoStep")
				.tasklet(prepareBLTempTitleAuthVideoTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthVideoStep")
	public Step blTempTitleAuthVideoStep() throws Exception {
		return steps.get("blTempTitleAuthVideoStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleAuthVideoStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthVideoPartitionedStep")
	public Step blTempTitleAuthVideoPartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthVideoPartitionedStep")
				.partitioner("blTempTitleAuthVideoPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthVideoStep())
				.build();
	}

	@Bean(name = "blTitleAuthVideo:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthVideoStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTH_VIDEO, "dedup_record_id", modulo);
	}

	/**
	 * merge MusicalScore records with same title and authority
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthMusicalScoreTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleAuthMusicalScoreTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleAuthMusicalScoreTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthMusicalScoreStep")
	public Step prepareBLTempTitleAuthMusicalScoreStep() {
		return steps.get("prepareBLTempTitleAuthMusicalScoreStep")
				.tasklet(prepareBLTempTitleAuthMusicalScoreTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthMusicalScoreStep")
	public Step blTempTitleAuthMusicalScoreStep() throws Exception {
		return steps.get("blTempTitleAuthMusicalScoreStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleAuthMusicalScoreStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthMusicalScorePartitionedStep")
	public Step blTempTitleAuthMusicalScorePartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthMusicalScorePartitionedStep")
				.partitioner("blTempTitleAuthMusicalScorePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthMusicalScoreStep())
				.build();
	}

	@Bean(name = "blTitleAuthMusicalScore:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthMusicalScoreStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTH_MUSICAL_SCORE, "dedup_record_id", modulo);
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
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleAuthorStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
				.retryLimit(10000)
				.reader(blTempTitleAuthorAudioStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
				.retryLimit(10000)
				.reader(blTempTitleAuthorVideoStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
				.retryLimit(10000)
				.reader(blTempTitleAuthorMusicalScoreStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthorMusicalScorePartitionedStep")
	public Step blTempTitleAuthorMusicalScorePartitionedStep() throws Exception {
		return steps.get("blTempTitleAuthorMusicalScorePartitionedStep")
				.partitioner("blTempTitleAuthorMusicalScorePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleAuthMusicalScoreStep())
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
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleLangPeriodicalStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempOrphanedStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
	 * biblioLinkerSimilarJob
	 */
	@Bean
	public Job biblioLinkerSimilarJob(
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":initBLSStep") Step initBLSStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthConspectusStep") Step prepareBLSimilarTempAuthConspectusStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthConspectusPartitionedStep") Step blSimilarTempAuthConspectusStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempConspectusStep") Step prepareBLSimilarTempConspectusStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempConspectusPartitionedStep") Step blSimilarTempConspectustep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthStep") Step prepareBLSimilarTempAuthStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthPartitionedStep") Step blSimilarTempAuthStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR)
				.validator(new DedupRecordsJobParametersValidator())
				.start(initBLSStep)
				.next(prepareBLSimilarTempAuthConspectusStep)
				.next(blSimilarTempAuthConspectusStep)
				.next(prepareBLSimilarTempConspectusStep)
				.next(blSimilarTempConspectustep)
				.next(prepareBLSimilarTempAuthStep)
				.next(blSimilarTempAuthStep)
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
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
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH_CONSPECTUS, "biblio_linker_id", modulo);
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
				.<List<Long>, List<HarvestedRecord>>chunk(1)
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
		return blSimpleKeysReader(TMP_BLS_TABLE_CONSPECTUS, "biblio_linker_id", modulo);
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
				.<List<Long>, List<HarvestedRecord>>chunk(1)
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
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH, "biblio_linker_id", modulo);
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
