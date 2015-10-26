package cz.mzk.recordmanager.server.index;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
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

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.Constants;

public class IndexIndividualRecordsToSolrJobTest extends AbstractTest {

	private static final String SOLR_URL = "http://localhost:8080/solr";

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
		reset(solrServerFactory);
		EmbeddedSolrServer server = createEmbeddedSolrServer();
		expect(solrServerFactory.create(SOLR_URL)).andReturn(server).anyTimes();
		replay(solrServerFactory);
		try {
			Job job = jobRegistry.getJob("indexIndividualRecordsToSolrJob");
			Map<String, JobParameter> params = new HashMap<String, JobParameter>();
			params.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(SOLR_URL));
			params.put(Constants.JOB_PARAM_RECORD_IDS, new JobParameter("MZK.MZK01-000000117"));
			JobParameters jobParams = new JobParameters(params);
			JobExecution execution = jobLauncher.run(job, jobParams);
			Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
			server.commit();
			
			{
				SolrQuery allDocsQuery = new SolrQuery();
				allDocsQuery.set("q", "id:60");
				allDocsQuery.set("rows", 10000);
				QueryResponse allDocsResponse = server.query(allDocsQuery);
				Assert.assertEquals(allDocsResponse.getResults().size(), 1);
			}
			
		} finally {
			server.shutdown();
		}
	}

	public EmbeddedSolrServer createEmbeddedSolrServer() throws Exception {
		File solrHome = new File("src/test/resources/solr/cores/");
		File configFile = new File(solrHome, "solr.xml");
		CoreContainer container = CoreContainer.createAndLoad(
				solrHome.getCanonicalPath(), configFile);
		EmbeddedSolrServer server = new EmbeddedSolrServer(container, "biblio");
		server.deleteByQuery("*:*");
		server.commit();
		return server;
	}

}
