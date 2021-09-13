package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.index.AbstractSolrTest;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KramAvailabilityIndexTest extends AbstractSolrTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/KramAvailabilityUrlTest.xml");
	}

	@Test
	public void indexKramAvailabilityUrlTest() throws Exception {
		SolrServerFacade server = solrServerFactory.create(SOLR_URL);
		Job job = jobRegistry.getJob("indexAllRecordsToSolrJob");
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_SOLR_URL, new JobParameter(SOLR_URL));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);

		{
			// different parent => no link
			SolrQuery docQuery = new SolrQuery();
			docQuery.set("q", SolrFieldConstants.ID_FIELD + ":1");
			QueryResponse docResponse = server.query(docQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			Assert.assertNull(document.get(SolrFieldConstants.URL));
		}

		{
			// same parent => link to issue
			SolrQuery docQuery = new SolrQuery();
			docQuery.set("q", SolrFieldConstants.ID_FIELD + ":2");
			QueryResponse docResponse = server.query(docQuery);
			Assert.assertEquals(docResponse.getResults().size(), 1);
			SolrDocument document = docResponse.getResults().get(0);
			Assert.assertEquals(document.get(SolrFieldConstants.URL),
					Collections.singletonList("KRAM-MZK|protected|https://www.digitalniknihovna.cz/mzk/uuid/uuid:4df7fb90-50de-11e9-abdc-5ef3fc9bb22f|Digitalizovaný dokument (č. 0)"));
		}
	}

}
