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
import org.springframework.jdbc.core.RowMapper;

import com.google.common.io.CharStreams;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.springbatch.SqlCommandTasklet;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class DedupRecordsJobConfig {
	

	
	private static final String TMP_TABLE_ISBN = "tmp_simmilar_books_isbn";
	
	private static final String TMP_TABLE_CNB = "tmp_simmilar_books_cnb";
	
	private static final String TMP_TABLE_CLUSTER = "tmp_cluster_ids";
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private HarvestedRecordDAO harvestedRecordDao;
    
    private String updateDedupRecordSql = CharStreams.toString(new InputStreamReader(getClass() //
    		.getClassLoader().getResourceAsStream("job/dedupRecordsJob/updateDedupRecord.sql"), "UTF-8"));
    
    private String deleteRecordLinkSql = CharStreams.toString(new InputStreamReader(getClass() //
    		.getClassLoader().getResourceAsStream("job/dedupRecordsJob/deleteRecordLink.sql"), "UTF-8"));
    
    private String prepareTempIsbnTableSql = CharStreams.toString(new InputStreamReader(getClass() //
    		.getClassLoader().getResourceAsStream("job/dedupRecordsJob/prepareTempIsbnTable.sql"), "UTF-8"));
    
    private String prepareTempCnbTableSql = CharStreams.toString(new InputStreamReader(getClass() //
    		.getClassLoader().getResourceAsStream("job/dedupRecordsJob/prepareTempCnbTable.sql"), "UTF-8"));
    
    private String prepareTempClusterIdSql = CharStreams.toString(new InputStreamReader(getClass() //
    		.getClassLoader().getResourceAsStream("job/dedupRecordsJob/prepareTempClusterId.sql"), "UTF-8"));
    
    
    public DedupRecordsJobConfig() throws IOException {
    }
    
    @Bean
    public Job dedupRecordsJob(
//    public Job dedupRecordsJob(@Qualifier("dedupRecordsJob:deleteStep") Step deleteStep,
//    		@Qualifier("dedupRecordsJob:updateStep") Step updateStep) {
//    		@Qualifier("dedupRecordsJob:deleteStep") Step deleteStep,
    		@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempClusterIdStep") Step prepareTempClusterIdStep,
    		@Qualifier(Constants.JOB_ID_DEDUP + ":dedupClusterIdsStep") 		Step dedupClusterIdsStep,
    		@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempIsbnTableStep") Step prepareTempIsbnTableStep,
    		@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnStep") 	Step dedupSimpleKeysISBNStep,
    		@Qualifier(Constants.JOB_ID_DEDUP + ":prepareTempCnbTableStep") 	Step prepareTempCnbTableStep,
    		@Qualifier(Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbStep") 	Step dedupSimpleKeysCnbStep,
    		@Qualifier(Constants.JOB_ID_DEDUP + ":dedupRestOfRecordsStep") 	Step dedupRestOfRecords
    		) {
        return jobs.get(Constants.JOB_ID_DEDUP)
        		.validator(new DedupRecordsJobParametersValidator())
        		.start(prepareTempClusterIdStep)
        		.next(dedupClusterIdsStep)
        		.next(prepareTempIsbnTableStep)
        		.next(dedupSimpleKeysISBNStep)
        		.next(prepareTempCnbTableStep)
        		.next(dedupSimpleKeysCnbStep)
        		.next(dedupRestOfRecords)
//        		.next(dropTempTablesStep)
				.build();
    }
    
//    @Bean(name="dedupRecordsJob:deleteStep")
//    public Step deleteStep() throws Exception {
//		return steps.get("dedupRecordsStep")
//				.tasklet(updateDedupRecordTasklet())
//				.build();
//    }
//    
//    @Bean(name="dedupRecordsJob:updateStep")
//    public Step step() throws Exception {
//		return steps.get("dedupRecordsUpdateStep")
//            .<HarvestedRecord, HarvestedRecord> chunk(100)
//            .reader(reader())
//            .writer(writer())
//            .build();
//    }
//    
    @Bean(name=Constants.JOB_ID_DEDUP + ":prepareTempIsbnTableStep")
    public Step prepareTempIsbnTableStep() {
    	return steps.get("prepareTempIsbnTableStep")
    			.listener(new StepProgressListener("prepareTempIsbnTableStep"))
				.tasklet(prepareTempIsbnTableTasklet())
				.build();
    }
    
    @Bean(name=Constants.JOB_ID_DEDUP + ":prepareTempCnbTableStep")
    public Step prepareTempCnbnTableStep() {
    	return steps.get("prepareTempCnbTableStep")
				.tasklet(prepareTempCnbTableTasklet())
				.listener(new StepProgressListener(":prepareTempCnbTableStep"))
				.build();
    }

    @Bean(name=Constants.JOB_ID_DEDUP + ":prepareTempClusterIdStep")
    public Step prepareTempClusterIdTableStep() {
    	return steps.get("prepareTempClusterIdStep")
				.tasklet(prepareTempClusterIdTasklet())
				.listener(new StepProgressListener("prepareTempClusterIdStep"))
				.build();
    }
    
    /**
     * This step deduplicates all records matching cluster id
     * @return
     * @throws Exception 
     */
    @Bean(name=Constants.JOB_ID_DEDUP + ":dedupClusterIdsStep")
    public Step dedupClusterIdsStep() throws Exception {
    	return steps.get("dedupClusterIdsStep")
    			.listener(new StepProgressListener("dedupClusterIdsStep"))
    			.<List<Long>, List<HarvestedRecord>> chunk(100)
    			.reader(dedupClusterIdReader())
    			.processor(dedupSimpleKeysStepProsessor())
    			.writer(dedupSimpleKeysStepWriter())
    			.build();
    }
    
    /**
     * This step deduplicates all books matching title, publication year and ISBN
     * @return
     * @throws Exception 
     */
    @Bean(name=Constants.JOB_ID_DEDUP + ":dedupSimpleKeysIsbnStep")
    public Step dedupSimpleKeysIsbnStep() throws Exception {
    	return steps.get("dedupSimpleKeysIsbnStep")
   			.listener(new StepProgressListener("dedupSimpleKeysIsbnStep"))
			.<List<Long>, List<HarvestedRecord>> chunk(100)
			.reader(dedupSimpleKeysIsbnReader())
			.processor(dedupSimpleKeysStepProsessor())
			.writer(dedupSimpleKeysStepWriter())
			.build();

    }
    
    /**
     * This step deduplicates all books matching title, publication year and CNB
     * @return
     * @throws Exception 
     */
    @Bean(name=Constants.JOB_ID_DEDUP + ":dedupSimpleKeysCnbStep")
    public Step dedupSimpleKeysCnbStep() throws Exception {
    	return steps.get("dedupSimpleKeysISBNStep")
    		.listener(new StepProgressListener("dedupSimpleKeysCnbStep"))
    	    .<List<Long>, List<HarvestedRecord>> chunk(100)
			.reader(dedupSimpleKeysCnbReader())
			.processor(dedupSimpleKeysStepProsessor())
			.writer(dedupSimpleKeysStepWriter())
			.build();

    }
    
    /**
     * This step assigns unique {@link DedupRecord} to each {@link HarvestedRecord} with missing {@link DedupRecord}
     * @return
     */
    @Bean(name=Constants.JOB_ID_DEDUP + ":dedupRestOfRecordsStep")
    public Step dedupRestOfRecords() throws Exception {
    	return steps.get("dedupRestOfRecords")
    	    .listener(new StepProgressListener("dedupRestOfRecords"))
    		.tasklet(dedupRestOfRecordsSqlTasklet())
			.build();
    }
    
    
    
//    @Bean(name="dedupRecordsJob:finalUpdateStep")
//    public Step finalUpdateStep() throws Exception {
//		return steps.get("dedupRecordsFinalUpdateStep")
//				.tasklet(updateDedupRecordTasklet())
//				.build();
//    }
    
	
    @Bean(name="dedupSimpleKeysIsbnStep:reader")
	@StepScope
    public ItemReader<List<Long>> dedupSimpleKeysIsbnReader () throws Exception {
    	return dedupSimpleKeysReader(TMP_TABLE_ISBN);
    }
    
    @Bean(name="dedupSimpleKeysCnbStep:reader")
	@StepScope
    public ItemReader<List<Long>> dedupSimpleKeysCnbReader () throws Exception {
    	return dedupSimpleKeysReader(TMP_TABLE_CNB);
    }
    
    @Bean(name="dedupClusterId:reader")
	@StepScope
    public ItemReader<List<Long>> dedupClusterIdReader () throws Exception {
    	return dedupSimpleKeysReader(TMP_TABLE_CLUSTER);
    }
    
    
    public ItemReader<List<Long>> dedupSimpleKeysReader (String tablename) throws Exception {
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
    
    @Bean(name="dedupSimpleKeysISBNStep:processor")
	@StepScope
	public ItemProcessor<List<Long>, List<HarvestedRecord>> dedupSimpleKeysStepProsessor() {
		return new DedupSimpleKeysStepProsessor();
	}
    
    @Bean(name="dedupSimpleKeysISBNStep:writer")
    @StepScope
    public ItemWriter<List<HarvestedRecord>> dedupSimpleKeysStepWriter () throws Exception {
    	return new DedupSimpleKeysStepWriter();
    }
    
    @Bean(name="prepareTempTablesStep:prepareTempIsbnTableTasklet")
	@StepScope
    public Tasklet prepareTempIsbnTableTasklet() {
    	return new SqlCommandTasklet(prepareTempIsbnTableSql);
    }
    
    @Bean(name="prepareTempTablesStep:prepareTempCnbTableTasklet")
	@StepScope
    public Tasklet prepareTempCnbTableTasklet() {
    	return new SqlCommandTasklet(prepareTempCnbTableSql);
    }
    
    @Bean(name="prepareTempTablesStep:prepareTempClusterIdTasklet")
	@StepScope
    public Tasklet prepareTempClusterIdTasklet() {
    	return new SqlCommandTasklet(prepareTempClusterIdSql);
    }
        
    @Bean(name="prepareTempTablesStep:dedupRestTasklet")
	@StepScope
    public Tasklet dedupRestOfRecordsSqlTasklet() {
    	return new SqlCommandTasklet("select dedup_rest_of_records()");
    }

    
    
    
    
    
    
    
    
    
    
//    @Bean(name="dedupRestOfRecordsStep:writer")
//   	@StepScope
//   	public ItemWriter<HarvestedRecord> dedupRestOfRecordsWriter() {
//    	return new DedupRestOfRecordsWriter();
//    }
//    
//    @Bean(name="dedupRestOfRecordsStep:reader")
//	@StepScope
//    public ItemReader<HarvestedRecord> dedupRestOfRecordsReader () throws Exception {
//    	JdbcPagingItemReader<HarvestedRecord> reader = new JdbcPagingItemReader<HarvestedRecord>();
//		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
//		pqpf.setDataSource(dataSource);
//		pqpf.setSelectClause("SELECT id,oai_harvest_conf_id,record_id,format");
//		pqpf.setFromClause("FROM harvested_record hr");
//		pqpf.setWhereClause("WHERE hr.dedup_record_id IS NULL");
//		pqpf.setSortKey("record_id");
//		reader.setRowMapper(rowMapper);
//		reader.setPageSize(20);
//    	reader.setQueryProvider(pqpf.getObject());
//    	reader.setDataSource(dataSource);
//    	reader.afterPropertiesSet();
//    	return reader;
//    }
    
    
    @Bean(name="dedupRecordsJob:processor")
	@StepScope
	public UpdateHarvestedRecordProcessor processor() {
		return new UpdateHarvestedRecordProcessor();
	}
    
    @Bean(name="dedupRecordsJob:writer")
	@StepScope
    public DedupRecordsWriter writer() {
    	return new DedupRecordsWriter();
    }
    
    @Bean(name="dedupRecordsJob:updateDedupRecordTasklet")
	@StepScope
    public Tasklet updateDedupRecordTasklet() {
    	return new SqlCommandTasklet(updateDedupRecordSql);
    }
    
    public class ArrayLongMapper implements RowMapper<List<Long>> {

		@Override
		public List<Long> mapRow(ResultSet arg0, int arg1)
				throws SQLException {
			List<Long> hrs = new ArrayList<>();
			
			String ids = arg0.getString("id_array");
			for (String idStr: ids.split(",")) {
				Long hrId = Long.valueOf(idStr);
				hrs.add(hrId);
			}
			
			return hrs;
		}
    	
    }
}
