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

	private static final String TMP_BL_TABLE_TITLE_TOPIC = "tmp_bl_title_topic";

	private static final String TMP_BL_TABLE_TITLE_TOPIC_AUDIO = "tmp_bl_title_topic_audio";

	private static final String TMP_BL_TABLE_TITLE_TOPIC_VIDEO = "tmp_bl_title_topic_video";

	private static final String TMP_BL_TABLE_REST_DEDUP = "tmp_bl_rest_dedup";

	private static final String TMP_BL_TABLE_ORPHANED = "tmp_bl_orphaned";

	private static final String TMP_BLS_TABLE_AUTH_COMMON_TITLE = "tmp_bls_auth_common_title";

	private static final String TMP_BLS_TABLE_AUTHOR_COMMON_TITLE = "tmp_bls_author_common_title";

	private static final String TMP_BLS_TABLE_AUTH_KEY_TITLE_LANG = "tmp_bls_auth_key_title_lang";

	private static final String TMP_BLS_TABLE_AUTHOR_TITLE_LANG = "tmp_bls_author_title_lang";

	private static final String TMP_BLS_TABLE_AUTH_KEY_TITLE = "tmp_bls_auth_key_title";

	private static final String TMP_BLS_TABLE_AUTHOR_TITLE = "tmp_bls_author_title";

	private static final String TMP_BLS_TABLE_TITLE_ENTITY_LANG = "tmp_bls_title_entity_lang";

	private static final String TMP_BLS_TABLE_TITLE_ENTITY_AUTH_KEY_LANG = "tmp_bls_title_entity_auth_key_lang";

	private static final String TMP_BLS_TABLE_TOPIC_KEY = "tmp_bls_topic_key";

	private static final String TMP_BLS_TABLE_AUTH_KEY_FORMAT_LANG = "tmp_bls_auth_key_format_lang";

	private static final String TMP_BLS_TABLE_AUTHOR_FORMAT_LANG = "tmp_bls_author_format_lang";

	private static final String TMP_BLS_TABLE_ENTITY_AUTH_KEY_LANG = "tmp_bls_entity_auth_key_lang";

	private static final String TMP_BLS_TABLE_ENTITY = "tmp_bls_entity";

	private static final String TMP_BLS_TABLE_TITLE_PLUS_LANG = "tmp_bls_title_plus_lang";

	private static final String TMP_BLS_TABLE_ISSN_SERIES = "tmp_bls_issn_series";

	private static final String TMP_BLS_TABLE_SERIES_PUBLISHER_LANG = "tmp_bls_series_publisher";

	private static final String TMP_BLS_TABLE_SOURCE_INFO_X_TOPIC_KEY = "tmp_bls_source_info_x_topic_key";

	private static final String TMP_BLS_TABLE_SOURCE_INFO_T_TOPIC_KEY = "tmp_bls_source_info_t_topic_key";

	private static final String TMP_BLS_TABLE_CONSPECTUS = "tmp_bls_conspectus";

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

	private String prepareBLTempTitleTopicTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleTopic.sql");

	private String prepareBLTempTitleTopicAudioTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleTopicAudio.sql");

	private String prepareBLTempTitleTopicVideoTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleTopicVideo.sql");

	private String prepareBLTempRestDedupTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempRestDedup.sql");

	private String prepareBLTempOrphanedTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempOrphaned.sql");

	private String prepareBLSimilarTempAuthCommonTitleTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthKeyCommonTitle.sql");

	private String prepareBLSimilarTempAuthorCommonTitleTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthorKeyCommonTitle.sql");

	private String prepareBLSimilarTempAuthKeyTitleLangTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthKeyTitleLang.sql");

	private String prepareBLSimilarTempAuthorTitleTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthorTitle.sql");

	private String prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthKeyTitleAudioMusicalScore.sql");

	private String prepareBLSimilarTempAuthorTitleAudioMusicalScoreTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthorTitleAudioMusicalScore.sql");

	private String prepareBLSimilarTempTitleEntityLangTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempTitleEntityLang.sql");

	private String prepareBLSimilarTempTitleEntityAuthKeyLangTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempTitleEntityAuthKeyLang.sql");

	private String prepareBLSimilarTempTopicKeyTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempTopicKey.sql");

	private String prepareBLSimilarTempAuthKeyFormatLangTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthKeyFormatLang.sql");

	private String prepareBLSimilarTempAuthorFormatLangTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuthorFormatLang.sql");

	private String prepareBLSimilarTempEntityTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempEntityLang.sql");

	private String prepareBLSimilarTempEntityAuthKeyLangTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempEntityAuthKeyLang.sql");

	private String prepareBLSimilarTempTitlePlusLangTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempTitlePlusLang.sql");

	private String prepareBLSimilarTempIssnSeriesTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempIssnSeries.sql");

	private String prepareBLSimilarTempSeriesPublisherTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempSeriesPublisher.sql");

	private String prepareBLSimilarTempSourceInfoXTopicKeyTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempSourceInfoXTopicKey.sql");

	private String prepareBLSimilarTempSourceInfoTTopicKeyTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempSourceInfoTTopicKey.sql");

	private String prepareBLSimilarTempConspectusTableSql =
			ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempConspectus.sql");

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
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicStep") Step prepareBLTempTitleTopicStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicPartitionedStep") Step blTempTitleTopicStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicAudioStep") Step prepareBLTempTitleTopicAudioStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicAudioPartitionedStep") Step blTempTitleTopicAudioStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicVideoStep") Step prepareBLTempTitleTopicVideoStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicVideoPartitionedStep") Step blTempTitleTopicVideoStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempRestDedupStep") Step prepareBLTempRestDedupStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempRestDedupPartitionedStep") Step blTempRestDedupStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempOrphanedStep") Step prepareBLTempOrphanedStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempOrphanedPartitionedStep") Step blTempOrphanedStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER)
				.validator(new DedupRecordsJobParametersValidator())
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
	 * merge books, maps, musical score records with same title, topic key, language and record format
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleTopicTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleTopicTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicStep")
	public Step prepareBLTempTitleTopicStep() {
		return steps.get("prepareBLTempTitleTopicStep")
				.tasklet(prepareBLTempTitleTopicTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicStep")
	public Step blTempTitleTopicStep() throws Exception {
		return steps.get("blTempTitleTopicStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleTopicStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicPartitionedStep")
	public Step blTempTitleTopicPartitionedStep() throws Exception {
		return steps.get("blTempTitleTopicPartitionedStep")
				.partitioner("blTempTitleTopicPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleTopicStep())
				.build();
	}

	@Bean(name = "blTitleTopic:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleTopicStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_TOPIC, "dedup_record_id", modulo);
	}

	/**
	 * merge audio records with same title, topic key
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicAudioTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleTopicAudioTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleTopicAudioTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicAudioStep")
	public Step prepareBLTempTitleTopicAudioStep() {
		return steps.get("prepareBLTempTitleTopicAudioStep")
				.tasklet(prepareBLTempTitleTopicAudioTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicAudioStep")
	public Step blTempTitleTopicAudioStep() throws Exception {
		return steps.get("blTempTitleTopicAudioStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleTopicAudioStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicAudioPartitionedStep")
	public Step blTempTitleTopicAudioPartitionedStep() throws Exception {
		return steps.get("blTempTitleTopicAudioPartitionedStep")
				.partitioner("blTempTitleTopicAudioPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleTopicAudioStep())
				.build();
	}

	@Bean(name = "blTitleTopicAudio:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleTopicAudioStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_TOPIC_AUDIO, "dedup_record_id", modulo);
	}

	/**
	 * merge video records with same title, topic key
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicVideoTasklet")
	@StepScope
	public Tasklet prepareBLTempTitleTopicVideoTasklet() {
		return new SqlCommandTasklet(prepareBLTempTitleTopicVideoTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleTopicVideoStep")
	public Step prepareBLTempTitleTopicVideoStep() {
		return steps.get("prepareBLTempTitleTopicVideoStep")
				.tasklet(prepareBLTempTitleTopicVideoTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicVideoStep")
	public Step blTempTitleTopicVideoStep() throws Exception {
		return steps.get("blTempTitleTopicVideoStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(100)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blTempTitleTopicVideoStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleTopicVideoPartitionedStep")
	public Step blTempTitleTopicVideoPartitionedStep() throws Exception {
		return steps.get("blTempTitleTopicVideoPartitionedStep")
				.partitioner("blTempTitleTopicVideoPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blTempTitleTopicVideoStep())
				.build();
	}

	@Bean(name = "blTitleTopicVideo:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleTopicVideoStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_TOPIC_VIDEO, "dedup_record_id", modulo);
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
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthCommonTitleStep") Step prepareBLSimilarTempAuthCommonTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthCommonTitlePartitionedStep") Step blSimilarTempAuthCommonTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorCommonTitleStep") Step prepareBLSimilarTempAuthorCommonTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorCommonTitlePartitionedStep") Step blSimilarTempAuthorCommonTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyTitleLangStep") Step prepareBLSimilarTempAuthKeyTitleLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyTitleLangPartitionedStep") Step blSimilarTempAuthKeyTitleLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorTitleStep") Step prepareBLSimilarTempAuthorTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorTitlePartitionedStep") Step blSimilarTempAuthorTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreStep") Step prepareBLSimilarTempAuthKeyTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyTitleAudioMusicalScorePartitionedStep") Step blSimilarTempAuthKeyTitleStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitleEntityLangStep") Step prepareBLSimilarTempTitleEntityLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitleEntityLangPartitionedStep") Step blSimilarTempTitleEntityLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitleEntityAuthKeyLangStep") Step prepareBLSimilarTempTitleEntityAuthKeyLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitleEntityAuthKeyLangPartitionedStep") Step blSimilarTempTitleEntityAuthKeyLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTopicKeyStep") Step prepareBLSimilarTempTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTopicKeyPartitionedStep") Step blSimilarTempTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyFormatLangStep") Step prepareBLSimilarTempAuthKeyFormatLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyFormatLangPartitionedStep") Step blSimilarTempAuthKeyFormatLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorFormatLangStep") Step prepareBLSimilarTempAuthorFormatLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorFormatLangPartitionedStep") Step blSimilarTempAuthorFormatLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityStep") Step prepareBLSimilarTempEntityStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityPartitionedStep") Step blSimilarTempEntityStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityAuthKeyLangStep") Step prepareBLSimilarTempEntityAuthKeyLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityAuthKeyLangPartitionedStep") Step blSimilarTempEntityAuthKeyLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitlePlusLangStep") Step prepareBLSimilarTempTitlePlusLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitlePlusLangPartitionedStep") Step blSimilarTempTitlePlusLangStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempIssnSeriesStep") Step prepareBLSimilarTempIssnSeriesStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempIssnSeriesPartitionedStep") Step blSimilarTempIssnSeriesStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSeriesPublisherStep") Step prepareBLSimilarTempSeriesPublisherStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSeriesPublisherPartitionedStep") Step blSimilarTempSeriesPublisherStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSourceInfoXTopicKeyStep") Step prepareBLSimilarTempSourceInfoXTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSourceInfoXTopicKeyPartitionedStep") Step blSimilarTempSourceInfoXTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSourceInfoTTopicKeyStep") Step prepareBLSimilarTempSourceInfoTTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSourceInfoTTopicKeyPartitionedStep") Step blSimilarTempSourceInfoTTopicKeyStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempConspectusStep") Step prepareBLSimilarTempConspectusStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempConspectusPartitionedStep") Step blSimilarTempConspectusStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR)
				.validator(new DedupRecordsJobParametersValidator())
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
				.next(prepareBLSimilarTempTopicKeyStep)
				.next(blSimilarTempTopicKeyStep)
				.next(prepareBLSimilarTempConspectusStep)
				.next(blSimilarTempConspectusStep)
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
	 * same autority, common title, language, format
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthCommonTitleTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthCommonTitleTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthCommonTitleTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthCommonTitleStep")
	public Step prepareBLSimilarTempAuthCommonTitleStep() {
		return steps.get("prepareBLSimilarTempAuthCommonTitleStep")
				.tasklet(prepareBLSimilarTempAuthCommonTitleTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthCommonTitleStep")
	public Step blSimilarTempAuthCommonTitleStep() throws Exception {
		return steps.get("blSimilarTempAuthCommonTitleStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthCommonTitleStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthCommonTitleStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthCommonTitlePartitionedStep")
	public Step blSimilarTempAuthCommonTitlePartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthCommonTitlePartitionedStep")
				.partitioner("blSimilarTempAuthCommonTitlePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthCommonTitleStep())
				.build();
	}

	@Bean(name = "blSimilarAuthCommonTitle:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthCommonTitleStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH_COMMON_TITLE, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthCommonTitle:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthCommonTitleStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTH_COMMON_TITLE);
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthorCommonTitleStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthorCommonTitleStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
	 * same auth key, title, language, for books, audio
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyTitleLangTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthKeyTitleLangTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthKeyTitleLangTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyTitleLangStep")
	public Step prepareBLSimilarTempAuthKeyTitleLangStep() {
		return steps.get("prepareBLSimilarTempAuthKeyTitleLangStep")
				.tasklet(prepareBLSimilarTempAuthKeyTitleLangTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyTitleLangStep")
	public Step blSimilarTempAuthKeyTitleLangStep() throws Exception {
		return steps.get("blSimilarTempAuthKeyTitleLangStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthKeyTitleLangStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthKeyTitleLangStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyTitleLangPartitionedStep")
	public Step blSimilarTempAuthKeyTitleLangPartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthKeyTitleLangPartitionedStep")
				.partitioner("blSimilarTempAuthKeyTitleLangPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthKeyTitleLangStep())
				.build();
	}

	@Bean(name = "blSimilarAuthKeyTitleLang:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthKeyTitleLangStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH_KEY_TITLE_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthKeyTitleLang:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthKeyTitleLangStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTH_TTILE_LANG);
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthorTitleStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthorTitleStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
	 * same authority, title, for audio, musical score
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreStep")
	public Step prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreStep() {
		return steps.get("prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreStep")
				.tasklet(prepareBLSimilarTempAuthKeyTitleAudioMusicalScoreTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyTitleAudioMusicalScoreStep")
	public Step blSimilarTempAuthKeyTitleAudioMusicalScoreStep() throws Exception {
		return steps.get("blSimilarTempAuthKeyTitleAudioMusicalScoreStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthKeyTitleAudioMusicalScoreStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthKeyTitleAudioMusicalScoreStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyTitleAudioMusicalScorePartitionedStep")
	public Step blSimilarTempAuthKeyTitleAudioMusicalScorePartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthKeyTitleAudioMusicalScorePartitionedStep")
				.partitioner("blSimilarTempAuthKeyTitleAudioMusicalScorePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthKeyTitleAudioMusicalScoreStep())
				.build();
	}

	@Bean(name = "blSimilarAuthKeyTitleAudioMusicalScore:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthKeyTitleAudioMusicalScoreStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH_KEY_TITLE, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthKeyTitleAudioMusicalScore:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthKeyTitleAudioMusicalScoreStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTH_TTILE);
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
				.retryLimit(10000)
				.reader(blSimilarTempAuthorTitleAudioMusicalScoreStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthorTitleAudioMusicalScoreStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
	 * same title, entity, language for books, video
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitleEntityLangTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempTitleEntityLangTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempTitleEntityLangTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitleEntityLangStep")
	public Step prepareBLSimilarTempTitleEntityLangStep() {
		return steps.get("prepareBLSimilarTempTitleEntityLangStep")
				.tasklet(prepareBLSimilarTempTitleEntityLangTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitleEntityLangStep")
	public Step blSimilarTempTitleEntityLangStep() throws Exception {
		return steps.get("blSimilarTempTitleEntityLangStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempTitleEntityLangStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarTitleEntityLangStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitleEntityLangPartitionedStep")
	public Step blSimilarTempTitleEntityLangPartitionedStep() throws Exception {
		return steps.get("blSimilarTempTitleEntityLangPartitionedStep")
				.partitioner("blSimilarTempTitleEntityLangPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempTitleEntityLangStep())
				.build();
	}

	@Bean(name = "blSimilarTitleEntityLang:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempTitleEntityLangStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_TITLE_ENTITY_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarTitleEntityLang:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarTitleEntityLangStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.TITLE_ENTITY_LANG);
	}

	/**
	 * same title, entity_auth_key, language for books, video
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitleEntityAuthKeyLangTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempTitleEntityAuthKeyLangTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempTitleEntityAuthKeyLangTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitleEntityAuthKeyLangStep")
	public Step prepareBLSimilarTempTitleEntityAuthKeyLangStep() {
		return steps.get("prepareBLSimilarTempTitleEntityAuthKeyLangStep")
				.tasklet(prepareBLSimilarTempTitleEntityAuthKeyLangTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitleEntityAuthKeyLangStep")
	public Step blSimilarTempTitleEntityAuthKeyLangStep() throws Exception {
		return steps.get("blSimilarTempTitleEntityAuthKeyLangStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempTitleEntityAuthKeyLangStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarTitleEntityAuthKeyLangStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitleEntityAuthKeyLangPartitionedStep")
	public Step blSimilarTempTitleEntityAuthKeyLangPartitionedStep() throws Exception {
		return steps.get("blSimilarTempTitleEntityAuthKeyLangPartitionedStep")
				.partitioner("blSimilarTempTitleEntityAuthKeyLangPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempTitleEntityAuthKeyLangStep())
				.build();
	}

	@Bean(name = "blSimilarTitleEntityAuthKeyLang:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempTitleEntityAuthKeyLangStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_TITLE_ENTITY_AUTH_KEY_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarTitleEntityAuthKeyLang:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarTitleEntityAuthKeyLangStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.TITLE_ENTITY_AUTH_KEY_LANG);
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempTopicKeyStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarTopicKeyStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
	 * same auth key, format, language
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyFormatLangTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthKeyFormatLangTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthKeyFormatLangTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthKeyFormatLangStep")
	public Step prepareBLSimilarTempAuthKeyFormatLangStep() {
		return steps.get("prepareBLSimilarTempAuthKeyFormatLangStep")
				.tasklet(prepareBLSimilarTempAuthKeyFormatLangTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyFormatLangStep")
	public Step blSimilarTempAuthKeyFormatLangStep() throws Exception {
		return steps.get("blSimilarTempAuthKeyFormatLangStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthKeyFormatLangStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthKeyFormatLangStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthKeyFormatLangPartitionedStep")
	public Step blSimilarTempAuthKeyFormatLangPartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthKeyFormatLangPartitionedStep")
				.partitioner("blSimilarTempAuthKeyFormatLangPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthKeyFormatLangStep())
				.build();
	}

	@Bean(name = "blSimilarAuthKeyFormatLang:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthKeyFormatLangStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTH_KEY_FORMAT_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthKeyFormatLang:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthKeyFormatLangStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTH_KEY_FORMAT_LANG);
	}

	/**
	 * same author, format, language
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorFormatLangTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempAuthorFormatLangTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempAuthorFormatLangTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempAuthorFormatLangStep")
	public Step prepareBLSimilarTempAuthorFormatLangStep() {
		return steps.get("prepareBLSimilarTempAuthorFormatLangStep")
				.tasklet(prepareBLSimilarTempAuthorFormatLangTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorFormatLangStep")
	public Step blSimilarTempAuthorFormatLangStep() throws Exception {
		return steps.get("blSimilarTempAuthorFormatLangStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempAuthorFormatLangStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarAuthorFormatLangStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempAuthorFormatLangPartitionedStep")
	public Step blSimilarTempAuthorFormatLangPartitionedStep() throws Exception {
		return steps.get("blSimilarTempAuthorFormatLangPartitionedStep")
				.partitioner("blSimilarTempAuthorFormatLangPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempAuthorFormatLangStep())
				.build();
	}

	@Bean(name = "blSimilarAuthorFormatLang:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempAuthorFormatLangStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_AUTHOR_FORMAT_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarAuthorFormatLang:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarAuthorFormatLangStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.AUTHOR_FORMAT_LANG);
	}

	/**
	 * same entity auth key, language
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityAuthKeyLangTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempEntityAuthKeyLangTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempEntityAuthKeyLangTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempEntityAuthKeyLangStep")
	public Step prepareBLSimilarTempEntityAuthKeyLangStep() {
		return steps.get("prepareBLSimilarTempEntityAuthKeyLangStep")
				.tasklet(prepareBLSimilarTempEntityAuthKeyLangTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityAuthKeyLangStep")
	public Step blSimilarTempEntityAuthKeyLangStep() throws Exception {
		return steps.get("blSimilarTempEntityAuthKeyLangStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempEntityAuthKeyLangStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarEntityAuthKeyLangStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempEntityAuthKeyLangPartitionedStep")
	public Step blSimilarTempEntityAuthKeyLangPartitionedStep() throws Exception {
		return steps.get("blSimilarTempEntityAuthKeyLangPartitionedStep")
				.partitioner("blSimilarTempEntityAuthKeyLangPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempEntityAuthKeyLangStep())
				.build();
	}

	@Bean(name = "blSimilarEntityAuthKeyLang:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempEntityAuthKeyLangStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_ENTITY_AUTH_KEY_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarEntityAuthKeyLang:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarEntityAuthKeyLangStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.ENTITY_AUTH_KEY_LANG);
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempEntityStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarEntityStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
	 * same title plus, language
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitlePlusLangTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempTitlePlusLangTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempTitlePlusLangTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempTitlePlusLangStep")
	public Step prepareBLSimilarTempTitlePlusLangStep() {
		return steps.get("prepareBLSimilarTempTitlePlusLangStep")
				.tasklet(prepareBLSimilarTempTitlePlusLangTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitlePlusLangStep")
	public Step blSimilarTempTitlePlusLangStep() throws Exception {
		return steps.get("blSimilarTempTitlePlusLangStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempTitlePlusLangStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarTitlePlusLangStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempTitlePlusLangPartitionedStep")
	public Step blSimilarTempTitlePlusLangPartitionedStep() throws Exception {
		return steps.get("blSimilarTempTitlePlusLangPartitionedStep")
				.partitioner("blSimilarTempTitlePlusLangPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempTitlePlusLangStep())
				.build();
	}

	@Bean(name = "blSimilarTitlePlusLang:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempTitlePlusLangStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_TITLE_PLUS_LANG, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarTitlePlusLang:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarTitlePlusLangStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.TITLE_PLUS_LANG);
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempIssnSeriesStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarIssnSeriesStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempSeriesPublisherStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarSeriesPublisherStepProsessor())
				.writer(blSimpleKeysStepWriter())
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
	 * same source_info_x, topic_key for articles
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSourceInfoXTopicKeyTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempSourceInfoXTopicKeyTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempSourceInfoXTopicKeyTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSourceInfoXTopicKeyStep")
	public Step prepareBLSimilarTempSourceInfoXTopicKeyStep() {
		return steps.get("prepareBLSimilarTempSourceInfoXTopicKeyStep")
				.tasklet(prepareBLSimilarTempSourceInfoXTopicKeyTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSourceInfoXTopicKeyStep")
	public Step blSimilarTempSourceInfoXTopicKeyStep() throws Exception {
		return steps.get("blSimilarTempSourceInfoXTopicKeyStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempSourceInfoXTopicKeyStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarSourceInfoXTopicKeyStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSourceInfoXTopicKeyPartitionedStep")
	public Step blSimilarTempSourceInfoXTopicKeyPartitionedStep() throws Exception {
		return steps.get("blSimilarTempSourceInfoXTopicKeyPartitionedStep")
				.partitioner("blSimilarTempSourceInfoXTopicKeyPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempSourceInfoXTopicKeyStep())
				.build();
	}

	@Bean(name = "blSimilarSourceInfoXTopicKey:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempSourceInfoXTopicKeyStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_SOURCE_INFO_X_TOPIC_KEY, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarSourceInfoXTopicKey:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarSourceInfoXTopicKeyStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.SOURCE_INFO_X_TOPIC_KEY);
	}

	/**
	 * same source_info_t, topic_key for articles
	 */
	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSourceInfoTTopicKeyTasklet")
	@StepScope
	public Tasklet prepareBLSimilarTempSourceInfoTTopicKeyTasklet() {
		return new SqlCommandTasklet(prepareBLSimilarTempSourceInfoTTopicKeyTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSimilarTempSourceInfoTTopicKeyStep")
	public Step prepareBLSimilarTempSourceInfoTTopicKeyStep() {
		return steps.get("prepareBLSimilarTempSourceInfoTTopicKeyStep")
				.tasklet(prepareBLSimilarTempSourceInfoTTopicKeyTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSourceInfoTTopicKeyStep")
	public Step blSimilarTempSourceInfoTTopicKeyStep() throws Exception {
		return steps.get("blSimilarTempSourceInfoTTopicKeyStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retryLimit(10000)
				.reader(blSimilarTempSourceInfoTTopicKeyStepReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(blSimilarSourceInfoTTopicKeyStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":blSimilarTempSourceInfoTTopicKeyPartitionedStep")
	public Step blSimilarTempSourceInfoTTopicKeyPartitionedStep() throws Exception {
		return steps.get("blSimilarTempSourceInfoTTopicKeyPartitionedStep")
				.partitioner("blSimilarTempSourceInfoTTopicKeyPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(blSimilarTempSourceInfoTTopicKeyStep())
				.build();
	}

	@Bean(name = "blSimilarSourceInfoTTopicKey:reader")
	@StepScope
	public ItemReader<List<Long>> blSimilarTempSourceInfoTTopicKeyStepReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo
	) throws Exception {
		return blSimpleKeysReader(TMP_BLS_TABLE_SOURCE_INFO_T_TOPIC_KEY, "biblio_linker_id", modulo);
	}

	@Bean(name = "blSimilarSourceInfoTTopicKey:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> blSimilarSourceInfoTTopicKeyStepProsessor() {
		return new BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType.SOURCE_INFO_T_TOPIC_KEY);
	}

	/**
	 * same bl_conspectus
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
				.<List<Long>, List<HarvestedRecord>>chunk(10)
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
