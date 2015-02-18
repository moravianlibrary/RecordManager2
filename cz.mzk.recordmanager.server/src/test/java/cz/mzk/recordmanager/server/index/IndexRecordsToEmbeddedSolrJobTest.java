package cz.mzk.recordmanager.server.index;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.DBUnitHelper;

public class IndexRecordsToEmbeddedSolrJobTest extends AbstractTest {

	private static final String SOLR_URL = "http://localhost:8080/solr";

	private DateFormat dateFormat = new SimpleDateFormat("d. MM. yyyy");

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private SolrServerFactory solrServerFactory;

	@Autowired
	private DBUnitHelper dbUnitHelper;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/IndexRecordsToSolrJobTest.xml");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void execute() throws Exception {
		reset(solrServerFactory);
		EmbeddedSolrServer server = createEmbeddedSolrServer();
		expect(solrServerFactory.create(SOLR_URL)).andReturn(server);
		replay(solrServerFactory);
		try {
			Job job = jobRegistry.getJob("indexRecordsToSolrJob");
			Map<String, JobParameter> params = new HashMap<String, JobParameter>();
			params.put("solrUrl", new JobParameter(SOLR_URL));
			JobParameters jobParams = new JobParameters(params);
			jobLauncher.run(job, jobParams);
			server.commit();
			SolrQuery query = new SolrQuery();
			query.set("q", "id:60");
			QueryResponse response = server.query(query);
			Assert.assertEquals(response.getResults().size(), 1);
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
