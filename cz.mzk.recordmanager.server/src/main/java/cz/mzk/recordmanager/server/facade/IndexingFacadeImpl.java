package cz.mzk.recordmanager.server.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;

@Component
public class IndexingFacadeImpl implements IndexingFacade {

	private static Logger logger = LoggerFactory.getLogger(IndexingFacadeImpl.class);

	private final String lastIndexedQuery = ResourceUtils.asString("sql/query/LastIndexedQuery.sql");

	private final String lastReindexedQuery = ResourceUtils.asString("sql/query/LastReindexedQuery.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private JobExecutor jobExecutor;

	@Value(value = "${solr.url:#{null}}")
	private String defaultSolrUrl;

	@Override
	public void index() {
		index(null);
	}

	@Override
	public void reindex() {
		reindex(null);
	}

	@Override
	public void index(String solrUrl) {
		executeIndexJob(Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS, solrUrl, false);
	}

	@Override
	public void reindex(String solrUrl) {
		executeIndexJob(Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS, solrUrl, true);
	}

	@Override
	public LocalDateTime getLastIndexed(String solrUrl) {
		return getLastIndexed(lastIndexedQuery, Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS, solrUrl);
	}

	@Override
	public void indexHarvestedRecords() {
		indexHarvestedRecords(null);
	}

	@Override
	public void reindexHarvestedRecords() {
		reindexHarvestedRecords(null);
	}

	@Override
	public void indexHarvestedRecords(String solrUrl) {
		executeIndexJob(Constants.JOB_ID_SOLR_INDEX_HARVESTED_RECORDS, solrUrl, false);
	}

	@Override
	public void reindexHarvestedRecords(String solrUrl) {
		executeIndexJob(Constants.JOB_ID_SOLR_INDEX_HARVESTED_RECORDS, solrUrl, true);
	}

	@Override
	public LocalDateTime getLastIndexedHarvestedRecords(String solrUrl) {
		return getLastIndexed(lastIndexedQuery, Constants.JOB_ID_SOLR_INDEX_HARVESTED_RECORDS, solrUrl);
	}

	@Override
	public LocalDateTime getLastReindexed(String solrUrl) {
		return getLastIndexed(lastReindexedQuery, Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS, solrUrl);
	}

	@Override
	public LocalDateTime getLastReindexedHarvestedRecords(String solrUrl) {
		return getLastIndexed(lastReindexedQuery, Constants.JOB_ID_SOLR_INDEX_HARVESTED_RECORDS, solrUrl);
	}

	private LocalDateTime getLastIndexed(String query, String jobName, String solrUrl) {
		List<Date> lastIndex = jdbcTemplate.queryForList(query, //
				ImmutableMap.of("jobName", jobName, "solrUrl", solrUrl), Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? LocalDateTime.ofInstant(lastIndex.get(0).toInstant(), ZoneId.systemDefault()) : null;
	}

	private void executeIndexJob(String jobName, String solrUrl, boolean reindex) {
		if (solrUrl == null) {
			if (defaultSolrUrl == null || defaultSolrUrl.isEmpty()) {
				throw new IllegalArgumentException("Parameter solrUrl is required, no default given");
			}
			solrUrl = defaultSolrUrl;
		}
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(solrUrl));
		if (!reindex) {
			LocalDateTime lastIndexed = (reindex) ? null : getLastIndexed(lastIndexedQuery, jobName, solrUrl);
			if (lastIndexed != null) {
				Date lastIndexedDate = Date.from(lastIndexed.atZone(ZoneId.systemDefault()).toInstant());
				logger.trace("Starting indexing from {}", lastIndexedDate);
				parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(lastIndexedDate));
			}
		}
		parameters.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(new Date()));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(jobName, params);
	}

}
