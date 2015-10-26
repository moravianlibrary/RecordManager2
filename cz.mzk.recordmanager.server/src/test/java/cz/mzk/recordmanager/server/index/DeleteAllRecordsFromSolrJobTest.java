package cz.mzk.recordmanager.server.index;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.easymock.EasyMock;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.Constants;

public class DeleteAllRecordsFromSolrJobTest extends AbstractTest {

	private static final String SOLR_URL = "http://localhost:8080/solr";

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private SolrServerFactory solrServerFactory;

	private SolrServerFacade mockedSolrServer = EasyMock.createMock(SolrServerFacade.class);

	@Test
	public void executeWitouthQuery() throws Exception {
		reset(solrServerFactory);
		reset(mockedSolrServer);
		expect(solrServerFactory.create(SOLR_URL)).andReturn(mockedSolrServer).anyTimes();
		mockedSolrServer.deleteByQuery("*:*");
		mockedSolrServer.commit();
		replay(solrServerFactory, mockedSolrServer);
		Job job = jobRegistry.getJob("deleteAllRecordsFromSolrJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(SOLR_URL, true));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
		verify(solrServerFactory, mockedSolrServer);
	}

	@Test
	public void executeWithQuery() throws Exception {
		String query = "id:MZK.MZK01-000000117";
		reset(solrServerFactory);
		reset(mockedSolrServer);
		expect(solrServerFactory.create(SOLR_URL)).andReturn(mockedSolrServer).anyTimes();
		mockedSolrServer.deleteByQuery(query);
		mockedSolrServer.commit();
		replay(solrServerFactory, mockedSolrServer);
		Job job = jobRegistry.getJob("deleteAllRecordsFromSolrJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(SOLR_URL, true));
		params.put(Constants.JOB_PARAM_SOLR_QUERY, new JobParameter(query, true));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
		verify(solrServerFactory, mockedSolrServer);
	}

}
