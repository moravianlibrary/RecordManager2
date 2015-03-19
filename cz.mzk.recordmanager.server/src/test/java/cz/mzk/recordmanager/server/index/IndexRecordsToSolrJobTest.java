package cz.mzk.recordmanager.server.index;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.verify;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.easymock.Capture;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.DBUnitHelper;

public class IndexRecordsToSolrJobTest extends AbstractTest {
	
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
	
	private SolrServer mockedSolrServer = EasyMock.createMock(SolrServer.class);
	
	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/IndexRecordsToSolrJobTest.xml");
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void execute() throws Exception {
		reset(solrServerFactory);
		reset(mockedSolrServer);
		expect(solrServerFactory.create(SOLR_URL)).andReturn(mockedSolrServer).anyTimes();
		Capture<Collection<SolrInputDocument>> documents1 = EasyMock.newCapture();
		//Capture<Collection<SolrInputDocument>> documents2 = EasyMock.newCapture();
		expect(mockedSolrServer.add(and(capture(documents1), (Collection<SolrInputDocument>) anyObject(Collection.class)), anyInt())).andReturn(new UpdateResponse());
		//expect(mockedSolrServer.add(and(capture(documents2), (Collection<SolrInputDocument>) anyObject(Collection.class)), anyInt())).andReturn(new UpdateResponse());
		replay(solrServerFactory, mockedSolrServer);
		
		Job job = jobRegistry.getJob("indexRecordsToSolrJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("from", new JobParameter(dateFormat.parse("1. 1. 2010")));
		params.put("to", new JobParameter(dateFormat.parse("1. 1. 2016")));
		params.put("solrUrl", new JobParameter(SOLR_URL));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
		verify(solrServerFactory, mockedSolrServer);
	}

}
