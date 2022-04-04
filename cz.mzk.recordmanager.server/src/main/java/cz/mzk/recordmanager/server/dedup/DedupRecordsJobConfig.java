package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.dedup.clustering.NonperiodicalTitleClusterable;
import cz.mzk.recordmanager.server.dedup.clustering.TitleClusterable;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.springbatch.DelegatingHibernateProcessor;
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
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
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
import java.util.*;
import java.util.concurrent.Future;

@Configuration
public class DedupRecordsJobConfig {

	private static final Integer INTEGER_OVERRIDEN_BY_EXPRESSION = null;

	private static final String TMP_TABLE_ISBN = "tmp_simmilar_books_isbn";

	private static final String TMP_TABLE_CNB = "tmp_simmilar_books_cnb";

	private static final String TMP_TABLE_EAN = "tmp_simmilar_ean";

	private static final String TMP_TABLE_BLIND_AUDIO = "tmp_simmilar_blind_audio";

	private static final String TMP_TABLE_PUBLISHER_NUMBER = "tmp_simmilar_publisher_number";

	private static final String TMP_TABLE_CLUSTER = "tmp_cluster_ids";

	private static final String TMP_TABLE_AUTH_TITLE = "tmp_auth_keys";

	private static final String TMP_TABLE_CNB_CLUSTERS = "tmp_cnb_clusters";

	private static final String TMP_TABLE_OCLC_CLUSTERS = "tmp_oclc_clusters";

	private static final String TMP_TABLE_UUID_CLUSTERS = "tmp_uuid_clusters";

	private static final String TMP_TABLE_ISMN_CLUSTERS = "tmp_ismn_clusters";

	private static final String TMP_TABLE_SIMILARITY_IDS = "tmp_similarity_ids";

	private static final String TMP_TABLE_SKAT_KEYS_MANUALLY_MERGED = "tmp_skat_keys_manually_merged";

	private static final String TMP_TABLE_SKAT_KEYS_REST = "tmp_skat_keys_rest";

	private static final String TMP_TABLE_PERIODICALS_ISSN = "tmp_simmilar_periodicals_issn";

	private static final String TMP_TABLE_PERIODICALS_CNB = "tmp_simmilar_periodicals_cnb";

	private static final String TMP_TABLE_PERIODICALS_CNB_CLUSTERS = "tmp_periodicals_cnb_clusters";

	private static final String TMP_TABLE_PERIODICALS_ISSN_CLUSTERS = "tmp_periodicals_issn_clusters";

	private static final String TMP_TABLE_PERIODICALS_OCLC_CLUSTERS = "tmp_periodicals_oclc_clusters";

	private static final String TMP_TABLE_PERIODICALS_SIMILARITY_IDS = "tmp_periodicals_similarity_ids";

	private static final String TMP_TABLE_PERIODICALS_SFX = "tmp_periodicals_sfx";

	private static final String TMP_TABLE_ARTICLES_XG = "tmp_simmilar_articles_xg";

	private static final String TMP_TABLE_ARTICLES_TG = "tmp_simmilar_articles_tg";

	private static final String TMP_TABLE_SFX_ID = "tmp_simmilar_sfx_id";

	private static final String TMP_TABLE_DISADVANTAGED_PUBLISHER = "tmp_disadvantaged_publisher";

	private static final String TMP_TABLE_DISADVANTAGED_EDITION = "tmp_disadvantaged_edition";

	private static final String TMP_TABLE_DISADVANTAGED_PAGES = "tmp_disadvantaged_pages";

	private static final String TMP_TABLE_DISADVANTAGED_ISBN = "tmp_disadvantaged_isbn";

	private static final String TMP_TABLE_DISADVANTAGED_CNB_PAGES = "tmp_disadvantaged_cnb_pages";

	private static final String TMP_TABLE_DISADVANTAGED_CNB_TITLE = "tmp_disadvantaged_cnb_title";

	private static final String TMP_TABLE_DISADVANTAGED_ISMN = "tmp_disadvantaged_Ismn";

	private static final String TMP_TABLE_BOOKPORT_ID = "tmp_simmilar_bookport_id";

	private final int partitionThreads = 6;

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

	private static final String initDeduplicationSql = ResourceUtils.asString("job/dedupRecordsJob/initDeduplication.sql");

	private static final String prepareTempIsbnTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempIsbnTable.sql");

	private static final String prepareTempCnbTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempCnbTable.sql");

	private static final String prepareTempEanTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempEanTable.sql");

	private static final String prepareTempBlindAudioTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempBlindAudioTable.sql");

	private static final String prepareTempPublisherNumberTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempPublisherNumberTable.sql");

	private static final String prepareTempClusterIdSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempClusterId.sql");

	private static final String prepareTempAuthKeyTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempAuthKeyTable.sql");

	private static final String prepareTempCnbClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempCnbClustersTable.sql");

	private static final String prepareTempOclcClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempOclcClustersTable.sql");

	private static final String prepareTempUuidClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempUuidClustersTable.sql");

	private static final String prepareTempIsmnClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempIsmnClustersTable.sql");

	private static final String prepareDedupSimmilarityTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareDedupSimmilarityTable.sql");

	private static final String prepareTempSkatKeysManuallyMerged = ResourceUtils.asString("job/dedupRecordsJob/prepareTempSkatManuallyMergedTable.sql");

	private static final String prepareTempSkatKeysRest = ResourceUtils.asString("job/dedupRecordsJob/prepareTempSkatRestTable.sql");

	private static final String uniqueRecordsDedupSql = ResourceUtils.asString("job/dedupRecordsJob/UniqueRecordsDedup.sql");

	private static final String prepareTempPeriodicalsIssnTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempPeriodicalsIssnTable.sql");

	private static final String prepareTempPeriodicalsCnbTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempPeriodicalsCnbTable.sql");

	private static final String prepareTempPeriodicalsCnbClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempPeriodicalsCnbClustersTable.sql");

	private static final String prepareTempPeriodicalsIssnClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempPeriodicalsIssnClustersTable.sql");

	private static final String prepareTempPeriodicalsOclcClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempPeriodicalsOclcClustersTable.sql");

	private static final String prepareTempPeriodicalsYearClustersSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempPeriodicalsYearCluster.sql");

	private static final String prepareTempPeriodicalsSfxSql = ResourceUtils.asString("job/dedupRecordsJob/prepareDedupSfxStep.sql");

	private static final String prepareTempArticlesXGTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempArticlesXGTable.sql");

	private static final String prepareTempArticlesTGTableSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempArticlesTGTable.sql");

	private static final String cleanupSql = ResourceUtils.asString("job/dedupRecordsJob/cleanup.sql");

	private static final String prepareTempSfxIdSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempSfxIdTable.sql");

	private static final String prepareTempDisadvantagedPublisherSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempDisadvantagedPublisherTable.sql");

	private static final String prepareTempDisadvantagedEditionSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempDisadvantagedEditionTable.sql");

	private static final String prepareTempDisadvantagedPagesSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempDisadvantagedPagesTable.sql");

	private static final String prepareTempDisadvantagedIsbnSql = ResourceUtils.asString("job/dedupRecordsJob/prepareTempDisadvantagedIsbnTable.sql");

	private static final String prepareTempDisadvantagedCnbPagesSql =
			ResourceUtils.asString("job/dedupRecordsJob/prepareTempDisadvantagedCnbPagesTable.sql");

	private static final String prepareTempDisadvantagedCnbTitleSql =
			ResourceUtils.asString("job/dedupRecordsJob/prepareTempDisadvantagedCnbTitleTable.sql");

	private static final String prepareTempDisadvantagedIsmnSql =
			ResourceUtils.asString("job/dedupRecordsJob/prepareTempDisadvantagedIsmnTable.sql");

	private static final String prepareTempBookportTableSql =
			ResourceUtils.asString("job/dedupRecordsJob/prepareTempBookportTable.sql");

	public DedupRecordsJobConfig() {
	}

	/**
	 * Dedup Job definition
	 */
	@Bean
	public Job dedupRecordsJob(
			@Qualifier(Constants.JOB_ID_DEDUP + ":initStep") Step initStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempClusterIdStep") Step prepareTempClusterIdStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupClusterIdsPartitionedStep") Step dedupClusterIdsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempSkatKeysManuallyMergedStep") Step prepareTempSkatKeysManuallyMergedStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatManuallyMergedPartitionedStep") Step dedupSimpleKeysSkatManuallyMergedStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempSfxIdTableStep") Step prepareTempSfxIdTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSfxIdStep") Step dedupSimpleKeysSfxIdStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempIsbnTableStep") Step prepareTempIsbnTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnPartitionedStep") Step dedupSimpleKeysISBNStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempCnbTableStep") Step prepareTempCnbTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbPartitionedStep") Step dedupSimpleKeysCnbStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempEanTableStep") Step prepareTempEanTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysEanStep") Step dedupSimpleKeysEanStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempBlindAudioTableStep") Step prepareTempBlindAudioTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysBlindAudioStep") Step dedupSimpleKeysBlindAudioStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempPublisherNumberTableStep") Step prepareTempPublisherNumberTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysPublisherNumberStep") Step dedupSimpleKeysPublisherNumberStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTmpTitleAuthStep") Step prepareTmpTitleAuthStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupTitleAuthPartitionedStep") Step dedupTitleAuthStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempCnbClustersTableStep") Step prepareTempCnbClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupCnbClustersPartitionedStep") Step dedupCnbClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempOclcClustersTableStep") Step prepareTempOclcClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupOclcClustersStep") Step dedupOclcClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempUuidClustersTableStep") Step prepareTempUuidClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupUuidClustersStep") Step dedupUuidClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempSkatKeysRestStep") Step prepareTempSkatKeysRestStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatRestPartitionedStep") Step dedupSimpleKeysSkatRestStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupSimmilarityTableStep") Step prepareDedupSimmilarityTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupSimmilarTitlesStep") Step prepareDedupSimmilarTitles,
			@Qualifier(Constants.JOB_ID_DEDUP + ":processSimilaritesResultsStep") Step processSimilaritesResultsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempIsmnClustersTableStep") Step prepareTempIsmnClustersTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupIsmnClustersStep") Step dedupIsmnClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupPeriodicalsIssnStep") Step prepareDedupPeriodicalsIssnStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupPeriodicalsIssnStep") Step dedupPeriodicalsIssnStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupPeriodicalsCnbStep") Step prepareDedupPeriodicalsCnbStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupPeriodicalsCnbStep") Step dedupPeriodicalsCnbStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":preparePeriodicalsCnbClustersStep") Step preparePeriodicalsCnbClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupPeriodicalsCnbClustersStep") Step dedupPeriodicalsCnbClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":preparePeriodicalsIssnClustersStep") Step preparePeriodicalsIssnClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupPeriodicalsIssnClustersStep") Step dedupPeriodicalsIssnClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":preparePeriodicalsOclcClustersStep") Step preparePeriodicalsOclcClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupPeriodicalsOclcClustersStep") Step dedupPeriodicalsOclcClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":preparePeriodicalsYearClustersStep") Step preparePeriodicalsYearClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareDedupPeriodicalsYearClustersStep") Step prepareDedupPeriodicalsYearClustersStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":processPeriodicalsSimilaritesResultsStep") Step processPeriodicalsSimilaritesResultsStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempArticlesXGTableStep") Step prepareTempArticlesXGTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupArticlesXGPartitionedStep") Step dedupArticlesXGStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempArticlesTGTableStep") Step prepareTempArticlesTGTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupArticlesTGPartitionedStep") Step dedupArticlesTGStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempBookportTableStep") Step prepareTempBookportTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupBookportStep") Step dedupBookportStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedPublisherTableStep") Step prepareTempDisadvantagedPublisherTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupDisadvantagedPublisherPartitionedStep") Step dedupDisadvantagedPublisherStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedEditionTableStep") Step prepareTempDisadvantagedEditionTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupDisadvantagedEditionPartitionedStep") Step dedupDisadvantagedEditionStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedPagesTableStep") Step prepareTempDisadvantagedPagesTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupDisadvantagedPagesPartitionedStep") Step dedupDisadvantagedPagesStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedIsbnTableStep") Step prepareTempDisadvantagedIsbnTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupDisadvantagedIsbnPartitionedStep") Step dedupDisadvantagedIsbnStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedCnbPagesTableStep") Step prepareTempDisadvantagedCnbPagesTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupDisadvantagedCnbPagesPartitionedStep") Step dedupDisadvantagedCnbPagesStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedCnbTitleTableStep") Step prepareTempDisadvantagedCnbTitleTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupDisadvantagedCnbTitlePartitionedStep") Step dedupDisadvantagedCnbTitleStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedIsmnTableStep") Step prepareTempDisadvantagedIsmnTableStep,
			@Qualifier(Constants.JOB_ID_DEDUP + ":dedupDisadvantagedIsmnPartitionedStep") Step dedupDisadvantagedIsmnStep,
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
				.next(prepareTempEanTableStep)
				.next(dedupSimpleKeysEanStep)
				.next(prepareTempBlindAudioTableStep)
				.next(dedupSimpleKeysBlindAudioStep)
				.next(prepareTempPublisherNumberTableStep)
				.next(dedupSimpleKeysPublisherNumberStep)
				.next(prepareTempCnbClustersTableStep)
				.next(dedupCnbClustersStep)
				.next(prepareTempOclcClustersTableStep)
				.next(dedupOclcClustersStep)
				.next(prepareTempIsmnClustersTableStep)
				.next(dedupIsmnClustersStep)
				.next(prepareDedupPeriodicalsIssnStep)
				.next(dedupPeriodicalsIssnStep)
				.next(prepareDedupPeriodicalsCnbStep)
				.next(dedupPeriodicalsCnbStep)
				.next(prepareTempUuidClustersTableStep)
				.next(dedupUuidClustersStep)
				.next(preparePeriodicalsCnbClustersStep)
				.next(dedupPeriodicalsCnbClustersStep)
				.next(preparePeriodicalsIssnClustersStep)
				.next(dedupPeriodicalsIssnClustersStep)
				.next(preparePeriodicalsOclcClustersStep)
				.next(dedupPeriodicalsOclcClustersStep)
				.next(prepareTempSkatKeysRestStep)
				.next(dedupSimpleKeysSkatRestStep)
				.next(prepareDedupSimmilarityTableStep)
				.next(prepareDedupSimmilarTitles)
				.next(processSimilaritesResultsStep)
				.next(preparePeriodicalsYearClustersStep)
				.next(prepareDedupPeriodicalsYearClustersStep)
				.next(processPeriodicalsSimilaritesResultsStep)
				.next(prepareTempArticlesXGTableStep)
				.next(dedupArticlesXGStep)
				.next(prepareTempArticlesTGTableStep)
				.next(dedupArticlesTGStep)
				.next(prepareTempSfxIdTableStep)
				.next(dedupSimpleKeysSfxIdStep)
				.next(prepareTempBookportTableStep)
				.next(dedupBookportStep)
				.next(prepareTempDisadvantagedPublisherTableStep)
				.next(dedupDisadvantagedPublisherStep)
				.next(prepareTempDisadvantagedEditionTableStep)
				.next(dedupDisadvantagedEditionStep)
				.next(prepareTempDisadvantagedPagesTableStep)
				.next(dedupDisadvantagedPagesStep)
				.next(prepareTempDisadvantagedIsbnTableStep)
				.next(dedupDisadvantagedIsbnStep)
				.next(prepareTempDisadvantagedCnbPagesTableStep)
				.next(dedupDisadvantagedCnbPagesStep)
				.next(prepareTempDisadvantagedCnbTitleTableStep)
				.next(dedupDisadvantagedCnbTitleStep)
				.next(prepareTempDisadvantagedIsmnTableStep)
				.next(dedupDisadvantagedIsmnStep)
				.next(dedupRestOfRecordsStep)
				.next(cleanupStep)
				.build();
	}

	/**
	 * Init deduplication
	 */
	@Bean(name="initTasklet")
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


	/**
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
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysClusterIdsReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupClusterIdsPartitionedStep")
	public Step dedupClusterIdsPartitionedStep() throws Exception {
		return steps.get("dedupClusterIdsPartitionedStep")
				.partitioner("dedupClusterIdsPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupClusterIdsStep())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysClusterIdsStepReader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysClusterIdsReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_CLUSTER, modulo);
	}

	/**
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
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysSkatManuallyMergedReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatManuallyMergedPartitionedStep")
	public Step dedupSimpleKeysSkatManuallyMergedPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysSkatManuallyMergedPartitionedStep")
				.partitioner("dedupSimpleKeysSkatManuallyMergedPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupSimpleKeysSkatManuallyMergedStep())
				.build();
	}

	@Bean(name = "dedupSimpleKeysSkatManuallyMergedStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysSkatManuallyMergedReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_SKAT_KEYS_MANUALLY_MERGED, modulo);
	}

	/**
	 * dedupSimpleKeysSfxStep Deduplicate all sfx
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempSfxIdTableTasklet")
	@StepScope
	public Tasklet prepareTempSfxIdTableTasklet() {
		return new SqlCommandTasklet(prepareTempSfxIdSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempSfxIdTableStep")
	public Step prepareTempSfxIdTableStep() {
		return steps.get("prepareTempSfxIdTableStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempSfxIdTableTasklet()).build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSfxIdStep")
	public Step dedupSimpleKeysSfxIdStep() throws Exception {
		return steps.get("dedupSimpleKeysSfxIdStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.reader(dedupSimpleKeysSfxIdReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter()).build();
	}

	@Bean(name = "dedupSimpleKeysSfxIdStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysSfxIdReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_SFX_ID, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	/**
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
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysIsbnReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnPartitionedStep")
	public Step dedupSimpleKeysIsbnPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysIsbnPartitionedStep")
				.partitioner("dedupSimpleKeysIsbnPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupSimpleKeysIsbnStep())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnStepReader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysIsbnReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_ISBN, modulo);
	}

	/**
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
		return steps.get("dedupSimpleKeysCnbStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysCnbReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbPartitionedStep")
	public Step dedupSimpleKeysCnbPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysCnbPartitionedStep")
				.partitioner("dedupSimpleKeysCnbPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupSimpleKeysCnbStep())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbStepReader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysCnbReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_CNB, modulo);
	}

	/**
	 * dedupSimpleKeysEanStep Deduplicate all books having equal publication
	 * year, EAN and title
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempEanTableTasklet")
	@StepScope
	public Tasklet prepareTempEanTableTasklet() {
		return new SqlCommandTasklet(prepareTempEanTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempEanTableStep")
	public Step prepareTempEanTableStep() {
		return steps.get("prepareTempEanTableStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempEanTableTasklet()).build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysEanStep")
	public Step dedupSimpleKeysEanStep() throws Exception {
		return steps.get("dedupSimpleKeysEanStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.reader(dedupSimpleKeysEanReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter()).build();
	}

	@Bean(name = "dedupSimpleKeysEanStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysEanReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_EAN, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	/**
	 * dedupSimpleKeysBlindAudioStep Deduplicate all audio
	 * year, author, title and record type = BLIND_AUDIO
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempBlindAudioTableTasklet")
	@StepScope
	public Tasklet prepareTempBlindAudioTableTasklet() {
		return new SqlCommandTasklet(prepareTempBlindAudioTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempBlindAudioTableStep")
	public Step prepareTempBlindAudioTableStep() {
		return steps.get("prepareTempBlindAudioTableStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempBlindAudioTableTasklet()).build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysBlindAudioStep")
	public Step dedupSimpleKeysBlindAudioStep() throws Exception {
		return steps.get("dedupSimpleKeysBlindAudioStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.reader(dedupSimpleKeysBlindAudioReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter()).build();
	}

	@Bean(name = "dedupSimpleKeysBlindAudioStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysBlindAudioReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_BLIND_AUDIO, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	/**
	 * dedupSimpleKeysPublisherNumberStep Deduplicate all books having equal publication
	 * year, publication_number and title
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempPublisherNumberTableTasklet")
	@StepScope
	public Tasklet prepareTempPublisherNumberTableTasklet() {
		return new SqlCommandTasklet(prepareTempPublisherNumberTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempPublisherNumberTableStep")
	public Step prepareTempPublisherNumberTableStep() {
		return steps.get("prepareTempPublisherNumberTableStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempPublisherNumberTableTasklet()).build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysPublisherNumberStep")
	public Step dedupSimpleKeysPublisherNumberStep() throws Exception {
		return steps.get("dedupSimpleKeysPublisherNumberStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.reader(dedupSimpleKeysPublisherNumberReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter()).build();
	}

	@Bean(name = "dedupSimpleKeysPublisherNumberStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysPublisherNumberReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_PUBLISHER_NUMBER, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	/**
	 * dedupArticlesXGStep Deduplicate audio having equal publication
	 * year, author, sourceinfo_x, sourceinfo_g and title
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempArticlesXGTableTasklet")
	@StepScope
	public Tasklet prepareTempArticlesXGTableTasklet() {
		return new SqlCommandTasklet(prepareTempArticlesXGTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempArticlesXGTableStep")
	public Step prepareTempArticlesXGTableStep() {
		return steps.get("prepareTempArticlesXGTableStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempArticlesXGTableTasklet()).build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupArticlesXGStep")
	public Step dedupArticlesXGStep() throws Exception {
		return steps.get("dedupArticlesXGStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupArticlesXGReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupArticlesXGPartitionedStep")
	public Step dedupArticlesXGPartitionedStep() throws Exception {
		return steps.get("dedupArticlesXGPartitionedStep")
				.partitioner("dedupArticlesXGPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupArticlesXGStep())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupArticlesXGStepReader")
	@StepScope
	public ItemReader<List<Long>> dedupArticlesXGReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_ARTICLES_XG, modulo);
	}

	/**
	 * dedupArticlesTGStep Deduplicate audio having equal publication
	 * year, author, sourceinfo_t, sourceinfo_g and title
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempArticlesTGTableTasklet")
	@StepScope
	public Tasklet prepareTempArticlesTGTableTasklet() {
		return new SqlCommandTasklet(prepareTempArticlesTGTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempArticlesTGTableStep")
	public Step prepareTempArticlesTGTableStep() {
		return steps.get("prepareTempArticlesTGTableStep")
				.listener(new StepProgressListener())
				.tasklet(prepareTempArticlesTGTableTasklet()).build();
	}


	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupArticlesTGStep")
	public Step dedupArticlesTGStep() throws Exception {
		return steps.get("dedupArticlesTGStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupArticlesTGReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupArticlesTGProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupArticlesTGPartitionedStep")
	public Step dedupArticlesTGPartitionedStep() throws Exception {
		return steps.get("dedupArticlesTGPartitionedStep")
				.partitioner("dedupArticlesTGPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupArticlesTGStep())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupArticlesTGStepReader")
	@StepScope
	public ItemReader<List<Long>> dedupArticlesTGReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_ARTICLES_TG, modulo);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupArticlesTGProcessor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupArticlesTGProcessor() {
		return new DedupArticlesTGProcessor(false);
	}

	/**
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
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(20000)
				.reader(dedupTitleAuthReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupTitleAuthProcessor())
				.writer(dedupSimpleKeysStepWriter()).build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupTitleAuthPartitionedStep")
	public Step dedupTitleAuthPartitionedStep() throws Exception {
		return steps.get("dedupTitleAuthPartitionedStep")
				.partitioner("slave", this.partioner()) //
				.gridSize(this.partitionThreads)
				.taskExecutor(this.taskExecutor)
				.step(dedupTitleAuthStep())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupTitleAuthReader")
	@StepScope
	public ItemReader<List<Long>> dedupTitleAuthReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_AUTH_TITLE, modulo);
	}

	@Bean(name = "dedupTitleAuthStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupTitleAuthProcessor() {
		return new DedupTitleAuthItemProcessor(false);
	}

	@Bean(name = "dedupRestOfRecordsStep:dedupRestTasklet")
	@StepScope
	public Tasklet dedupRestOfRecordsSqlTasklet() {
		return new UniqueRecordsDedupTasklet(uniqueRecordsDedupSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupRestOfRecordsStep")
	public Step dedupRestOfRecordsStep() {
		return steps.get("dedupRestOfRecordsStep")
				.tasklet(dedupRestOfRecordsSqlTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	/**
	 * 	Deduplicate same CNB
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

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupCnbClustersStep")
	public Step dedupCnbClustersStep() throws Exception {
		return steps.get("dedupCnbClustersStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupCnbClustersReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupCNBClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupCnbClustersPartitionedStep")
	public Step dedupCnbClustersPartitionedStep() throws Exception {
		return steps.get("dedupCnbClustersPartitionedStep")
				.partitioner("dedupCnbClustersPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupCnbClustersStep())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupCnbClustersStepReader")
	@StepScope
	public ItemReader<List<Long>> dedupCnbClustersReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_CNB_CLUSTERS, modulo);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupCNBClustersProcessor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupCNBClustersProcessor() {
		return new DedupIdentifierCNBClustersProcessor(false);
	}

	/**
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
		return dedupSimpleKeysReader(TMP_TABLE_OCLC_CLUSTERS, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupOclcClustersStep")
	public Step dedupOclcClustersStep() throws Exception {
		return steps.get("dedupOclcClustersTableStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.reader(dedupOclcClustersReader())
				.processor(generalDedupClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
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
		return dedupSimpleKeysReader(TMP_TABLE_UUID_CLUSTERS, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupUuidClustersStep")
	public Step dedupUuidClustersStep() throws Exception {
		return steps.get("dedupUuidClustersStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.reader(dedupUuidClustersReader())
				.processor(generalDedupClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * Deduplicate bookport records
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempBookportTableTasklet")
	@StepScope
	public Tasklet prepareBookportTasklet() {
		return new SqlCommandTasklet(prepareTempBookportTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempBookportTableStep")
	public Step prepareTempBookportTableStep() {
		return steps.get("prepareTempBookportTableStep")
				.tasklet(prepareBookportTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "dedupBookportStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupBookportReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_BOOKPORT_ID, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupBookportStep")
	public Step dedupBookportStep() throws Exception {
		return steps.get("dedupBookportStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.reader(dedupBookportReader())
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
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
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysSkatRestReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSkatKeysProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupSimpleKeysSkatRestPartitionedStep")
	public Step dedupSimpleKeysSkatRestPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysSkatRestPartitionedStep")
				.partitioner("slave", this.partioner()) //
				.gridSize(this.partitionThreads)
				.taskExecutor(this.taskExecutor)
				.step(dedupSimpleKeysSkatRestStep())
				.build();
	}

	@Bean(name = "dedupSimpleKeysSkatRestStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysSkatRestReader(@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_SKAT_KEYS_REST, modulo);
	}

	/**
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
	public ItemReader<List<NonperiodicalTitleClusterable>> yearReader() {
		return new TitleByYearReader();
	}

	@Bean(name="prepareDedupSimmilarTitlesStep:titleProcessor")
	public ItemProcessor<List<NonperiodicalTitleClusterable>,List<Set<Long>>> titleProcessor() {
		return new SimilarTitleProcessor<>();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareDedupSimmilarTitlesStep")
	public Step prepareDedupSimmilarTitles() throws Exception {
		return steps.get("prepareDedupSimmilarTitlesStep")
				.listener(new StepProgressListener())
				.<List<NonperiodicalTitleClusterable>, Future<List<Set<Long>>>> chunk(10)
				.reader(yearReader())
				.processor(asyncSimmilarityProcessor())
				.writer(asyncSimmilarityWriter())
				.build();
	}

	@Bean(name ="prepareDedupSimmilarTitlesStep:asynprepareDedupSimmilarTitlesProcessor")
	@StepScope
	public AsyncItemProcessor<List<NonperiodicalTitleClusterable>, List<Set<Long>>> asyncSimmilarityProcessor() {
		AsyncItemProcessor<List<NonperiodicalTitleClusterable>, List<Set<Long>>> processor = new AsyncItemProcessor<>();
		processor.setDelegate(new DelegatingHibernateProcessor<>(sessionFactory, titleProcessor()));
		processor.setTaskExecutor(taskExecutor);
		return processor;
	}


	@Bean(name="prepareDedupSimmilarTitlesStep:asyncSimmilarityWriter")
	@StepScope
	public AsyncItemWriter<List<Set<Long>>> asyncSimmilarityWriter() throws Exception {
		AsyncItemWriter<List<Set<Long>>> writer = new AsyncItemWriter<>();
		writer.setDelegate(simpleSimmilarityWriter());
		writer.afterPropertiesSet();
		return writer;
	}

	@Bean(name="prepareDedupSimmilarTitlesStep:simpleSimmilarityWriter")
	@StepScope
	public ItemWriter<List<Set<Long>>> simpleSimmilarityWriter() {
		return new TitleSimilarityWriter(TMP_TABLE_SIMILARITY_IDS);
	}

	/**
	 * Process computed similarities results
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":processSimilaritesResultsStep")
	public Step processSimilaritesResultsStep() throws Exception {
		return steps.get("processSimilaritesResultsStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.reader(dedupSimpleKeysReader(TMP_TABLE_SIMILARITY_IDS, INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * 	Deduplicate same ISMN
	 */
	@Bean(name = "prepareTempTablesStep:prepareTempIsmnClustersTableTasklet")
	@StepScope
	public Tasklet prepareIsmnClustersTasklet() {
		return new SqlCommandTasklet(prepareTempIsmnClustersSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempIsmnClustersTableStep")
	public Step prepareTempIsmnClustersTableStep() {
		return steps.get("prepareTempIsmnClustersTableStep")
				.tasklet(prepareIsmnClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "dedupIsmnClustersStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupIsmnClustersReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_ISMN_CLUSTERS, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupIsmnClustersStep")
	public Step dedupIsmnClustersStep() throws Exception {
		return steps.get("dedupIsmnClustersTableStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(10)
				.reader(dedupIsmnClustersReader())
				.processor(generalDedupClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * Deduplicate periodicals using ISSN and title
	 */

	@Bean(name = "prepareDedupPeriodicalsIssnStep:prepareDedupPeriodicalsIssnTasklet")
	@StepScope
	public Tasklet prepareDedupPeriodicalsIssnTasklet() {
		return new SqlCommandTasklet(prepareTempPeriodicalsIssnTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareDedupPeriodicalsIssnStep")
	public Step prepareDedupPeriodicalsIssnStep() {
		return steps.get("prepareDedupPeriodicalsIssnStep")
				.tasklet(prepareDedupPeriodicalsIssnTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "dedupPeriodicalsIssnStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupPeriodicalsIssnReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_PERIODICALS_ISSN, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = "dedupPeriodicalsIssnStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupPeriodicalsIssnProcessor() {
		return new DedupSimpleKeysStepProcessor(false);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupPeriodicalsIssnStep")
	public Step dedupPeriodicalsIssnStep() throws Exception {
		return steps.get("dedupPeriodicalsIssnStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(50)
				.reader(dedupPeriodicalsIssnReader())
				.processor(dedupPeriodicalsIssnProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * Deduplicate periodicals using CNB and title
	 */

	@Bean(name = "prepareDedupPeriodicalsCnbStep:prepareDedupPeriodicalsCnbTasklet")
	@StepScope
	public Tasklet prepareDedupPeriodicalsCnbTasklet() {
		return new SqlCommandTasklet(prepareTempPeriodicalsCnbTableSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareDedupPeriodicalsCnbStep")
	public Step prepareDedupPeriodicalsCnbStep() {
		return steps.get("prepareDedupPeriodicalsCnbStep")
				.tasklet(prepareDedupPeriodicalsCnbTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "dedupPeriodicalsCnbStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupPeriodicalsCnbReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_PERIODICALS_CNB, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = "dedupPeriodicalsCnbStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupPeriodicalsCnbProcessor() {
		return new DedupSimpleKeysStepProcessor(false);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupPeriodicalsCnbStep")
	public Step dedupPeriodicalsCnbStep() throws Exception {
		return steps.get("dedupPeriodicalsCnbStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(50)
				.reader(dedupPeriodicalsCnbReader())
				.processor(dedupPeriodicalsCnbProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * Deduplicate periodicals using Cnb clusters
	 */

	@Bean(name = "preparePeriodicalsCnbClustersStep:preparePeriodicalsCnbClustersTasklet")
	@StepScope
	public Tasklet preparePeriodicalsCnbClustersTasklet() {
		return new SqlCommandTasklet(prepareTempPeriodicalsCnbClustersSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":preparePeriodicalsCnbClustersStep")
	public Step preparePeriodicalsCnbClustersStep() {
		return steps.get("preparePeriodicalsCnbClustersStep")
				.tasklet(preparePeriodicalsCnbClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "dedupPeriodicalsCnbClustersStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupPeriodicalsCnbClustersStepReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_PERIODICALS_CNB_CLUSTERS, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupPeriodicalsCnbClustersStep")
	public Step dedupPeriodicalsCnbClustersStep() throws Exception {
		return steps.get("dedupPeriodicalsCnbClustersStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(50)
				.reader(dedupPeriodicalsCnbClustersStepReader())
				.processor(generalDedupClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * Deduplicate periodicals using Issn clusters
	 */

	@Bean(name = "preparePeriodicalsIssnClustersStep:preparePeriodicalsIssnClustersTasklet")
	@StepScope
	public Tasklet preparePeriodicalsIssnClustersTasklet() {
		return new SqlCommandTasklet(prepareTempPeriodicalsIssnClustersSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":preparePeriodicalsIssnClustersStep")
	public Step preparePeriodicalsIssnClustersStep() {
		return steps.get("preparePeriodicalsIssnClustersStep")
				.tasklet(preparePeriodicalsIssnClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "dedupPeriodicalsIssnClustersStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupPeriodicalsIssnClustersStepReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_PERIODICALS_ISSN_CLUSTERS, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupPeriodicalsIssnClustersStep")
	public Step dedupPeriodicalsIssnClustersStep() throws Exception {
		return steps.get("dedupPeriodicalsIssnClustersStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(50)
				.reader(dedupPeriodicalsIssnClustersStepReader())
				.processor(generalDedupClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * Deduplicate periodicals using Oclc clusters
	 */

	@Bean(name = "preparePeriodicalsOclcClustersStep:preparePeriodicalsOclcClustersTasklet")
	@StepScope
	public Tasklet preparePeriodicalsOclcClustersTasklet() {
		return new SqlCommandTasklet(prepareTempPeriodicalsOclcClustersSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":preparePeriodicalsOclcClustersStep")
	public Step preparePeriodicalsOclcClustersStep() {
		return steps.get("preparePeriodicalsOclcClustersStep")
				.tasklet(preparePeriodicalsOclcClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = "dedupPeriodicalsOclcClustersStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupPeriodicalsOclcClustersStepReader() throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_PERIODICALS_OCLC_CLUSTERS, INTEGER_OVERRIDEN_BY_EXPRESSION);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupPeriodicalsOclcClustersStep")
	public Step dedupPeriodicalsOclcClustersStep() throws Exception {
		return steps.get("dedupPeriodicalsOclcClustersStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(50)
				.reader(dedupPeriodicalsOclcClustersStepReader())
				.processor(generalDedupClustersProcessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * Deduplicate periodicals by year
	 */

	@Bean(name = "preparePeriodicalsYearClustersStep:preparePeriodicalsYearClustersTasklet")
	@StepScope
	public Tasklet preparePeriodicalsYearClustersTasklet() {
		return new SqlCommandTasklet(prepareTempPeriodicalsYearClustersSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":preparePeriodicalsYearClustersStep")
	public Step preparePeriodicalsYearClustersStep() {
		return steps.get("preparePeriodicalsYearClustersStep")
				.tasklet(preparePeriodicalsYearClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name="preparePeriodicalsYearClustersStep:yearReader")
	public ItemReader<List<TitleClusterable>> preparePeriodicalsYearClustersStepReader() {
		return new PeriodicalsTitleByYearReader(1850,2015);
	}

	@Bean(name="preparePeriodicalsYearClustersStep:titleProcessor")
	public ItemProcessor<List<TitleClusterable>,List<Set<Long>>> preparePeriodicalsYearClustersStepProcessor() {
		return new SimilarTitleProcessor<>();
	}

	@Bean(name="preparePeriodicalsYearClustersStep:writer")
	@StepScope
	public ItemWriter<List<Set<Long>>> preparePeriodicalsYearClustersStepWriter() {
		return new TitleSimilarityWriter(TMP_TABLE_PERIODICALS_SIMILARITY_IDS);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareDedupPeriodicalsYearClustersStep")
	public Step preparePeriodicalsDedupSimmilarTitles() throws Exception {
		return steps.get("prepareDedupPeriodicalsYearClustersStep")
				.listener(new StepProgressListener())
				.<List<TitleClusterable>, Future<List<Set<Long>>>> chunk(10)
				.reader(preparePeriodicalsYearClustersStepReader())
				.processor(asyncPeriodicalsSimilarityProcessor())
				.writer(asyncPeriodicalsSimmilarityWriter())
				.build();
	}

	@Bean(name ="prepareDedupPeriodicalsYearClustersStep:asynprepareDedupSimmilarTitlesProcessor")
	@StepScope
	public AsyncItemProcessor<List<TitleClusterable>, List<Set<Long>>> asyncPeriodicalsSimilarityProcessor() {
		AsyncItemProcessor<List<TitleClusterable>, List<Set<Long>>> processor = new AsyncItemProcessor<>();
		processor.setDelegate(new DelegatingHibernateProcessor<>(sessionFactory, preparePeriodicalsYearClustersStepProcessor()));
		processor.setTaskExecutor(taskExecutor);
		return processor;
	}


	@Bean(name="prepareDedupPeriodicalsYearClustersStep:asyncSimmilarityWriter")
	@StepScope
	public AsyncItemWriter<List<Set<Long>>> asyncPeriodicalsSimmilarityWriter() throws Exception {
		AsyncItemWriter<List<Set<Long>>> writer = new AsyncItemWriter<>();
		writer.setDelegate(preparePeriodicalsYearClustersStepWriter());
		writer.afterPropertiesSet();
		return writer;
	}

	/**
	 * Process computed similarities results for periodicals
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":processPeriodicalsSimilaritesResultsStep")
	public Step processPeriodicalsSimilaritesResultsStep() throws Exception {
		return steps.get("processPeriodicalsSimilaritesResultsStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(50)
				.reader(dedupSimpleKeysReader(TMP_TABLE_PERIODICALS_SIMILARITY_IDS, INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupPeriodicalsSimilaritesResultsSteprocessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = "processPeriodicalsSimilaritesResultsStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupPeriodicalsSimilaritesResultsSteprocessor() {
		return new DedupPeriodicalsSimilaritesResultsProcessor(false);
	}

	/**
	 * Deduplicate periodicals from for SFX-NLK
	 */
	@Bean(name = "preparePeriodicalsSfxTasklet:preparePeriodicalsSfxTasklet")
	@StepScope
	public Tasklet preparePeriodicalsSfxNlkTasklet() {
		return new SqlCommandTasklet(prepareTempPeriodicalsSfxSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":preparePeriodicalsSfxNlkStep")
	public Step preparePeriodicalsSfxNlk() {
		return steps.get("preparePeriodicalsNlkStep")
				.tasklet(preparePeriodicalsYearClustersTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupPeriodicalsSfxStep")
	public Step dedupPeriodicalsSfxStep() throws Exception {
		return steps.get("dedupPeriodicalsSfxStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>> chunk(50)
				.reader(dedupSimpleKeysReader(TMP_TABLE_PERIODICALS_SFX, INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupSimpleKeysStepProsessor())
				.writer(dedupSimpleKeysStepWriter())
				.build();
	}

	/**
	 * prepareTempDisadvantagedPublisherTableStep Deduplicate all disadvantaged records with same short_title,
	 * author_string, publisher, publication_year
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedPublisherTasklet")
	@StepScope
	public Tasklet prepareTempDisadvantagedPublisherTasklet() {
		return new SqlCommandTasklet(prepareTempDisadvantagedPublisherSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedPublisherTableStep")
	public Step prepareTempDisadvantagedPublisherStep() {
		return steps.get("prepareTempDisadvantagedPublisherStep")
				.tasklet(prepareTempDisadvantagedPublisherTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedPublisherStep")
	public Step dedupDisadvantagedPublisherStep() throws Exception {
		return steps.get("dedupDisadvantagedPublisherStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysDisadvantagedPublisherReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupDisadvantagedKeysStepProsessor())
				.writer(dedupDisadvantagedKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedPublisherPartitionedStep")
	public Step dedupSimpleKeysDisadvantagedPublisherPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysDisadvantagedPublisherPartitionedStep")
				.partitioner("dedupSimpleKeysDisadvantagedPublisherPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupDisadvantagedPublisherStep())
				.build();
	}

	@Bean(name = "dedupDisadvantagedPublisherStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysDisadvantagedPublisherReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_DISADVANTAGED_PUBLISHER, modulo);
	}

	/**
	 * prepareTempDisadvantagedEditionTableStep Deduplicate all disadvantaged records with same short_title,
	 * author_string, edition, publication_year
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedEditionTasklet")
	@StepScope
	public Tasklet prepareTempDisadvantagedEditionTasklet() {
		return new SqlCommandTasklet(prepareTempDisadvantagedEditionSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedEditionTableStep")
	public Step prepareTempDisadvantagedEditionStep() {
		return steps.get("prepareTempDisadvantagedEditionStep")
				.tasklet(prepareTempDisadvantagedEditionTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedEditionStep")
	public Step dedupDisadvantagedEditionStep() throws Exception {
		return steps.get("dedupDisadvantagedEditionStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysDisadvantagedEditionReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupDisadvantagedKeysStepProsessor())
				.writer(dedupDisadvantagedKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedEditionPartitionedStep")
	public Step dedupSimpleKeysDisadvantagedEditionPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysDisadvantagedEditionPartitionedStep")
				.partitioner("dedupSimpleKeysDisadvantagedEditionPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupDisadvantagedEditionStep())
				.build();
	}

	@Bean(name = "dedupDisadvantagedEditionStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysDisadvantagedEditionReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_DISADVANTAGED_EDITION, modulo);
	}

	/**
	 * prepareTempDisadvantagedPagesTableStep Deduplicate all disadvantaged records with same short_title,
	 * author_string, pages, publication_year
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedPagesTasklet")
	@StepScope
	public Tasklet prepareTempDisadvantagedPagesTasklet() {
		return new SqlCommandTasklet(prepareTempDisadvantagedPagesSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedPagesTableStep")
	public Step prepareTempDisadvantagedPagesStep() {
		return steps.get("prepareTempDisadvantagedPagesStep")
				.tasklet(prepareTempDisadvantagedPagesTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedPagesStep")
	public Step dedupDisadvantagedPagesStep() throws Exception {
		return steps.get("dedupDisadvantagedPagesStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(1)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysDisadvantagedPagesReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupDisadvantagedKeysStepProsessor())
				.writer(dedupDisadvantagedKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedPagesPartitionedStep")
	public Step dedupSimpleKeysDisadvantagedPagesPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysDisadvantagedPagesPartitionedStep")
				.partitioner("dedupSimpleKeysDisadvantagedPagesPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupDisadvantagedPagesStep())
				.build();
	}

	@Bean(name = "dedupDisadvantagedPagesStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysDisadvantagedPagesReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_DISADVANTAGED_PAGES, modulo);
	}

	/**
	 * prepareTempDisadvantagedIsbnTableStep Deduplicate all disadvantaged records with same isbn,
	 * anp_title, pages, publication_year
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedIsbnTasklet")
	@StepScope
	public Tasklet prepareTempDisadvantagedIsbnTasklet() {
		return new SqlCommandTasklet(prepareTempDisadvantagedIsbnSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedIsbnTableStep")
	public Step prepareTempDisadvantagedIsbnStep() {
		return steps.get("prepareTempDisadvantagedIsbnStep")
				.tasklet(prepareTempDisadvantagedIsbnTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedIsbnStep")
	public Step dedupDisadvantagedIsbnStep() throws Exception {
		return steps.get("dedupDisadvantagedIsbnStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysDisadvantagedIsbnReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupDisadvantagedKeysStepProsessor())
				.writer(dedupDisadvantagedKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedIsbnPartitionedStep")
	public Step dedupSimpleKeysDisadvantagedIsbnPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysDisadvantagedIsbnPartitionedStep")
				.partitioner("dedupSimpleKeysDisadvantagedIsbnPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupDisadvantagedIsbnStep())
				.build();
	}

	@Bean(name = "dedupDisadvantagedIsbnStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysDisadvantagedIsbnReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_DISADVANTAGED_ISBN, modulo);
	}

	/**
	 * prepareTempDisadvantagedCnbPagesTableStep Deduplicate all disadvantaged records with same cnb,
	 * pages, without format {@link HarvestedRecordFormatEnum#MUSICAL_SCORES}
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedCnbPagesTasklet")
	@StepScope
	public Tasklet prepareTempDisadvantagedCnbPagesTasklet() {
		return new SqlCommandTasklet(prepareTempDisadvantagedCnbPagesSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedCnbPagesTableStep")
	public Step prepareTempDisadvantagedCnbPagesStep() {
		return steps.get("prepareTempDisadvantagedCnbPagesStep")
				.tasklet(prepareTempDisadvantagedCnbPagesTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedCnbPagesStep")
	public Step dedupDisadvantagedCnbPagesStep() throws Exception {
		return steps.get("dedupDisadvantagedCnbPagesStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysDisadvantagedCnbPagesReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupDisadvantagedKeysStepProsessor())
				.writer(dedupDisadvantagedKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedCnbPagesPartitionedStep")
	public Step dedupSimpleKeysDisadvantagedCnbPagesPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysDisadvantagedCnbPagesPartitionedStep")
				.partitioner("dedupSimpleKeysDisadvantagedCnbPagesPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupDisadvantagedCnbPagesStep())
				.build();
	}

	@Bean(name = "dedupDisadvantagedCnbPagesStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysDisadvantagedCnbPagesReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_DISADVANTAGED_CNB_PAGES, modulo);
	}

	/**
	 * prepareTempDisadvantagedCnbTitleTableStep Deduplicate all disadvantaged records with same cnb,
	 * title
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedCnbTitleTasklet")
	@StepScope
	public Tasklet prepareTempDisadvantagedCnbTitleTasklet() {
		return new SqlCommandTasklet(prepareTempDisadvantagedCnbTitleSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedCnbTitleTableStep")
	public Step prepareTempDisadvantagedCnbTitleStep() {
		return steps.get("prepareTempDisadvantagedCnbTitleStep")
				.tasklet(prepareTempDisadvantagedCnbTitleTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedCnbTitleStep")
	public Step dedupDisadvantagedCnbTitleStep() throws Exception {
		return steps.get("dedupDisadvantagedCnbTitleStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysDisadvantagedCnbTitleReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupDisadvantagedKeysStepProsessor())
				.writer(dedupDisadvantagedKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedCnbTitlePartitionedStep")
	public Step dedupSimpleKeysDisadvantagedCnbTitlePartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysDisadvantagedCnbTitlePartitionedStep")
				.partitioner("dedupSimpleKeysDisadvantagedCnbTitlePartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupDisadvantagedCnbTitleStep())
				.build();
	}

	@Bean(name = "dedupDisadvantagedCnbTitleStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysDisadvantagedCnbTitleReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_DISADVANTAGED_CNB_TITLE, modulo);
	}

	/**
	 * prepareTempDisadvantagedIsmnTableStep Deduplicate all disadvantaged records with same ismn,
	 * publication_year, pages, pages greater than 3
	 */
	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedIsmnTasklet")
	@StepScope
	public Tasklet prepareTempDisadvantagedIsmnTasklet() {
		return new SqlCommandTasklet(prepareTempDisadvantagedIsmnSql);
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":prepareTempDisadvantagedIsmnTableStep")
	public Step prepareTempDisadvantagedIsmnStep() {
		return steps.get("prepareTempDisadvantagedIsmnStep")
				.tasklet(prepareTempDisadvantagedIsmnTasklet())
				.listener(new StepProgressListener())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedIsmnStep")
	public Step dedupDisadvantagedIsmnStep() throws Exception {
		return steps.get("dedupDisadvantagedIsmnStep")
				.listener(new StepProgressListener())
				.<List<Long>, List<HarvestedRecord>>chunk(10)
				.faultTolerant()
				.keyGenerator(KeyGeneratorForList.INSTANCE)
				.retry(LockAcquisitionException.class)
				.retry(OptimisticLockException.class)
				.retryLimit(10000)
				.reader(dedupSimpleKeysDisadvantagedIsmnReader(INTEGER_OVERRIDEN_BY_EXPRESSION))
				.processor(dedupDisadvantagedKeysStepProsessor())
				.writer(dedupDisadvantagedKeysStepWriter())
				.build();
	}

	@Bean(name = Constants.JOB_ID_DEDUP + ":dedupDisadvantagedIsmnPartitionedStep")
	public Step dedupSimpleKeysDisadvantagedIsmnPartitionedStep() throws Exception {
		return steps.get("dedupSimpleKeysDisadvantagedIsmnPartitionedStep")
				.partitioner("dedupSimpleKeysDisadvantagedIsmnPartitionedStepSlave", this.partioner()) //
				.taskExecutor(this.taskExecutor)
				.gridSize(this.partitionThreads)
				.step(dedupDisadvantagedIsmnStep())
				.build();
	}

	@Bean(name = "dedupDisadvantagedIsmnStep:reader")
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysDisadvantagedIsmnReader(
			@Value("#{stepExecutionContext[modulo]}") Integer modulo) throws Exception {
		return dedupSimpleKeysReader(TMP_TABLE_DISADVANTAGED_ISMN, modulo);
	}

	/**
	 * Cleanup
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

	/**
	 * Generic components
	 */
	@StepScope
	public ItemReader<List<Long>> dedupSimpleKeysReader(String tablename, @Value("#{stepExecutionContext[modulo]}") Integer modulo)
			throws Exception {
		JdbcPagingItemReader<List<Long>> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT row_id, id_array");
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

	@Bean(name = "dedupSimpleKeys:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupSimpleKeysStepProsessor() {
		return new DedupSimpleKeysStepProcessor(false);
	}

	@Bean(name = "dedupDisadvantagedKeys:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupDisadvantagedKeysStepProsessor() {
		return new DedupSimpleKeysStepProcessor(true);
	}

	@Bean(name = "dedupSimpleKeys:writer")
	@StepScope
	public ItemWriter<List<HarvestedRecord>> dedupSimpleKeysStepWriter() {
		return new DedupSimpleKeysStepWriter();
	}

	@Bean(name = "dedupDisadvantagedKeys:writer")
	@StepScope
	public ItemWriter<List<HarvestedRecord>> dedupDisadvantagedKeysStepWriter() {
		return new DedupDisadvantagedKeysStepWriter();
	}

	/**
	 * general processor for deduplication of clusters based on identifier (ISBN,ISSN,CNB,OCLC,ISMN)
	 */
	@Bean(name = "generalDedupClustersProcessor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> generalDedupClustersProcessor() {
		return new DedupIdentifierClustersProcessor(false);
	}

	/**
	 * Record processor for deduplication of records based on Skat data
	 * @return {@link DedupSkatKeysProcessor}
	 */
	@Bean(name = "dedupSkatKeys:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupSkatKeysProcessor() {
		return new DedupSkatKeysProcessor(false);
	}

	@Bean(name=Constants.JOB_ID_DEDUP + ":dedupSimpleKeysPartioner")
	@StepScope
	public IntegerModuloPartitioner partioner() {
		return new IntegerModuloPartitioner();
	}

	public class ArrayLongMapper implements RowMapper<List<Long>> {

		@Override
		public List<Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
			String ids = rs.getString("id_array");
			String[] idStrs = ids.split(",");
			List<Long> hrs = new ArrayList<>(idStrs.length);
			for (String idStr : idStrs) {
				Long hrId = Long.valueOf(idStr);
				hrs.add(hrId);
			}
			return hrs;
		}

	}

}
