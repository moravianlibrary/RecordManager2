package cz.mzk.recordmanager.server.index;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.anyObject;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.easymock.EasyMock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;

public class IndexRecordsToSolrJobTest extends AbstractTest {
	
	private static final String SOLR_URL = "http://localhost:8080/solr";
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private SolrServerFactory solrServerFactory;
	
	private SolrServer mockedSolrServer = EasyMock.createMock(SolrServer.class);
	
	@Test
	@SuppressWarnings("unchecked")
	public void execute() throws Exception {
		reset(solrServerFactory);
		reset(mockedSolrServer);
		expect(solrServerFactory.create(SOLR_URL)).andReturn(mockedSolrServer);
		expect(mockedSolrServer.add(anyObject(Collection.class))).andReturn(new UpdateResponse());
		replay(solrServerFactory, mockedSolrServer);
		
		Job job = jobRegistry.getJob("indexRecordsToSolrJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("from", new JobParameter(new Date()));
		params.put("to", new JobParameter(new Date()));
		params.put("solrUrl", new JobParameter(SOLR_URL));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

}
