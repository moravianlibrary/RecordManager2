package cz.mzk.recordmanager.server.index;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.Constants;

public class IndexHarvestedRecordsToEmbeddedSolrJobTest extends AbstractSolrTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private SolrServerFactory solrServerFactory;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/IndexRecordsToSolrJobTest.xml");
	}

	@Test
	public void execute() throws Exception {
		Job job = jobRegistry.getJob("indexHarvestedRecordsToSolrJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(SOLR_URL));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
	}

}
