package cz.mzk.recordmanager.server.facade;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;

@Component
public class IndexingFacadeImpl implements IndexingFacade {

	private static Logger logger = LoggerFactory.getLogger(IndexingFacadeImpl.class);

	private final String lastCompletedHarvestQuery = ResourceUtils.asString("sql/query/LastIndexedQuery.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void index(String solrUrl) {
		Date lastIndexed = getLastIndexed(solrUrl);
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(solrUrl));
		if (lastIndexed != null) {
			logger.trace("Starting indexing from {}", lastIndexed);
			parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(lastIndexed));
		}
		parameters.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(new Date()));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS, params);
	}

	@Override
	public Date getLastIndexed(String solrUrl) {
		return jdbcTemplate.queryForObject(lastCompletedHarvestQuery, //
				ImmutableMap.of("solrUrl", solrUrl), Date.class);
	}

}
