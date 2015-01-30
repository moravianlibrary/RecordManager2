package cz.mzk.recordmanager.server.oai.harvest;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;

public class OAIHarvestJobTest extends AbstractTest {
	
	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private OAIHarvestConfigurationDAO configDao;
	
	@Test
	public void execute() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecords1.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/ListRecords2.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21")).andReturn(response1);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&resumptionToken=201408211302186999999999999999MZK01-VDK%3AMZK01-VDK")).andReturn(response2);
		replay(httpClient);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("configurationId", new JobParameter(300L));
		JobParameters jobParams = new JobParameters(params);
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", jobParams);
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
	}
	
	@Test
	public void testIdentify() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecords1.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/ListRecords2.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21")).andReturn(response1);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&resumptionToken=201408211302186999999999999999MZK01-VDK%3AMZK01-VDK")).andReturn(response2);
		replay(httpClient);

		// prepare oaiConfig
		final Long configId = 300L;
		OAIHarvestConfiguration oaiConfig = configDao.get(configId);
		oaiConfig.setGranularity(null);
		configDao.persist(oaiConfig);
		
		oaiConfig = configDao.get(configId);

		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("configurationId", new JobParameter(configId));
		params.put("dummyparam", new JobParameter("dummyParam"));
		
		JobParameters jobParams = new JobParameters(params);
		
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", jobParams);
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);

		final OAIHarvestConfiguration updatedOaiConfig = configDao.get(configId);

		// is granularity correctly read from Identify?
		final String expectedGranularity = "YYYY-MM-DDThh:mm:ssZ";
		Assert.assertEquals(updatedOaiConfig.getGranularity(), expectedGranularity);
	}
	
	@Test 
	public void testHarvestFromDate() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecordsFrom.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&from=2015-01-01T01%3A00%3A00Z&metadataPrefix=marc21")).andReturn(response1);
		replay(httpClient);
		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(new Date(1420070400000L)));
			
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
	}

	
	@Test 
	public void testHarvestUntilDate() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecordsUntil.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21&until=2014-12-09T01%3A00%3A00Z")).andReturn(response1);
		replay(httpClient);
			
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(new Date(1418083200000L)));
			
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
	}
	
	@Test 
	public void testTimeGranularityConversion() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/IdentifyNonstandardGranularity.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecordsNLK.xml");
		expect(httpClient.executeGet("http://oai.medvik.cz/medvik2cpk/oai?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://oai.medvik.cz/medvik2cpk/oai?verb=ListRecords&from=2015-01-01&metadataPrefix=marc21")).andReturn(response1);
		replay(httpClient);

		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(301L));
		params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(new Date(1420070400000L)));
			
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
	}

}
