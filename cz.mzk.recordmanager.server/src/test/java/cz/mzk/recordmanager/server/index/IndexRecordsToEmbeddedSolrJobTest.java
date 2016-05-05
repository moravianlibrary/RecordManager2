package cz.mzk.recordmanager.server.index;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
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

import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.Constants;

public class IndexRecordsToEmbeddedSolrJobTest extends AbstractSolrTest {

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
		SolrServerFacade server = solrServerFactory.create(SOLR_URL);
		Job job = jobRegistry.getJob("indexAllRecordsToSolrJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(SOLR_URL));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);

		{
			SolrQuery allDocsQuery = new SolrQuery();
			allDocsQuery.set("q", "id:*");
			allDocsQuery.set("rows", 10000);
			QueryResponse allDocsResponse = server.query(allDocsQuery);
			Assert.assertEquals(allDocsResponse.getResults().size(), 79);
		}

		{
			SolrQuery docQuery = new SolrQuery();
			docQuery.set("q", "id:64");
			QueryResponse docResponse = server.query(docQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			Assert.assertEquals(document.get("author"), "Grisham, John, 1955-");
			Assert.assertEquals(document.get("title"), "--a je čas zabíjet /");
		}

		{
			SolrQuery docQuery = new SolrQuery();
			docQuery.set("q", "id:91");
			QueryResponse docResponse = server.query(docQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			Assert.assertEquals(document.get("author"), "Andrić, Ivo, 1892-1975");
			Assert.assertEquals(document.get("title"), "Most přes Drinu");
		}

		{
			SolrQuery deletedDocQuery = new SolrQuery();
			deletedDocQuery.set("q", "id:99");
			QueryResponse docResponse = server.query(deletedDocQuery);
			Assert.assertEquals(docResponse.getResults().size(), 0);
		}

		{
			SolrQuery authQuery = new SolrQuery();
			authQuery.set("q", "id:73");
			QueryResponse docResponse = server.query(authQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			// check whether author_search field contains alternative name from authority record
			Assert.assertTrue(
					document.getFieldValues("author_viz").stream() //
							.anyMatch(s -> s.equals("Imaginarni, Karel, 1900-2000")), 
					"Authority enrichment failed.");
		}

		{
			SolrQuery dcQuery = new SolrQuery();
			dcQuery.set("q", "id:100");
			QueryResponse docResponse = server.query(dcQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			Assert.assertTrue(document.containsKey("recordtype"), "Record type missing in DC record.");
			Assert.assertEquals(document.getFieldValue("recordtype"), "dublinCore");
			Assert.assertTrue(document.containsKey("fullrecord"), "Full record missing in DC record.");
			String fullRecord = (String) document.getFieldValue("fullrecord");
			Assert.assertTrue(fullRecord.length() > 0);
		}

		{
			// check for url
			SolrQuery dcQuery = new SolrQuery();
			dcQuery.set("q", "id:96");
			QueryResponse docResponse = server.query(dcQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			Assert.assertTrue(
					document.getFieldValues("url").stream()
						.anyMatch(url -> url.equals("MZK|unknown|http://krameriusndktest.mzk.cz/search/handle/uuid:f1401080-de25-11e2-9923-005056827e52|Digitalizovaný dokument"))
			);
		}

		{
			// check for Kramerius url
			SolrQuery dcQuery = new SolrQuery();
			dcQuery.set("q", "id:100");
			QueryResponse docResponse = server.query(dcQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			Assert.assertTrue(
					document.getFieldValues("url").stream()
						.anyMatch(url -> url.equals("KRAM-MZK|online|http://kramerius.mzk.cz/search/i.jsp?pid=UUID:039764f8-d6db-11e0-b2cd-0050569d679d|"))
			);
		}

		{
			// check for indexed fulltext
			SolrQuery dcQuery = new SolrQuery();
			dcQuery.set("q", "id:100 AND fulltext:indexace");
			QueryResponse docResponse = server.query(dcQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
		}

		{
			// check for indexed fulltext
			SolrQuery dcQuery = new SolrQuery();
			dcQuery.set("q", "id:100 AND fulltext:neexistuje");
			QueryResponse docResponse = server.query(dcQuery);
			Assert.assertEquals(docResponse.getResults().size(), 0);
		}

	}

}
