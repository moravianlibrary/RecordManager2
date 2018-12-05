package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.dedup.DedupRecordsJobParametersValidator;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;
import cz.mzk.recordmanager.server.springbatch.StepProgressListener;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.hibernate.SessionFactory;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

	private String initBiblioLinkerSql = ResourceUtils.asString("job/biblioLinkerJob/initBiblioLinker.sql");

	private String prepareBLTempTitleAuthTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLTempTitleAuth.sql");

	private String prepareBLSTempAuthTableSql = ResourceUtils.asString("job/biblioLinkerJob/prepareBLSTempAuth.sql");

	@Bean
	public Job biblioLinkerJob(
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":initBLStep") Step initBLStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":prepareBLTempTitleAuthStep") Step prepareBLTempTitleAuthStep,
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER + ":blTempTitleAuthStep") Step blTempTitleAuthStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER)
				.validator(new DedupRecordsJobParametersValidator())
				.start(initBLStep)
				.next(prepareBLTempTitleAuthStep)
				.next(blTempTitleAuthStep)
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
	 *
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
				.reader(blTempTitleAuthStepReader())
				.processor(blSimpleKeysStepProsessor())
				.writer(blSimpleKeysStepWriter())
				.build();
	}

	@Bean(name = "blTitleAuth:reader")
	@StepScope
	public ItemReader<List<Long>> blTempTitleAuthStepReader() throws Exception {
		return blSimpleKeysReader(TMP_BL_TABLE_TITLE_AUTH);
	}

	/**
	 *
	 */
	@Bean
	public Job biblioLinkerSimilarJob(
			@Qualifier(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSTempAuthStep") Step prepareBLSTempAuthStep
	) {
		return jobs.get(Constants.JOB_ID_BIBLIO_LINKER_SIMILAR)
				.validator(new DedupRecordsJobParametersValidator())
				.start(prepareBLSTempAuthStep)
				.build();
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSTempAuthTasklet")
	@StepScope
	public Tasklet prepareBLSTempAuthTasklet() {
		return new SqlCommandTasklet(prepareBLSTempAuthTableSql);
	}

	@Bean(name = Constants.JOB_ID_BIBLIO_LINKER_SIMILAR + ":prepareBLSTempAuthStep")
	public Step prepareBLSTempAuthStep() {
		return steps.get("prepareBLSTempAuthStep")
				.tasklet(prepareBLSTempAuthTasklet())
				.listener(new StepProgressListener())
				.build();
	}


	/**
	 * Generic components
	 */
	private ItemReader<List<Long>> blSimpleKeysReader(String tablename)
			throws Exception {
		JdbcPagingItemReader<List<Long>> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT row_id,dedup_record_id");
		pqpf.setFromClause("FROM " + tablename);
		pqpf.setSortKey("row_id");
		reader.setRowMapper(new ArrayLongMapper());
		reader.setPageSize(100);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
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

	public class ArrayLongMapper implements RowMapper<List<Long>> {

		@Override
		public List<Long> mapRow(ResultSet arg0, int arg1) throws SQLException {
			List<Long> hrs = new ArrayList<>();
			String ids = arg0.getString("dedup_record_id");
			for (String idStr : ids.split(",")) {
				Long hrId = Long.valueOf(idStr);
				hrs.add(hrId);
			}
			return hrs;
		}
	}
}
