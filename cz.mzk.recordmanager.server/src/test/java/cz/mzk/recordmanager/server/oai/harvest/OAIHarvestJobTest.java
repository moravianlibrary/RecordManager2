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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIGranularity;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
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
	
	@Autowired
	private HarvestedRecordDAO recordDao;
	
	@BeforeMethod
	public void initLocator() throws Exception {
		dbUnitHelper.init("dbunit/OAIHarvestTest.xml");
	}
	
	@Test
	public void testIdentify() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecords2.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21")).andReturn(response1);
		replay(httpClient);

		final Long configId = 300L;
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("configurationId", new JobParameter(configId));
		JobParameters jobParams = new JobParameters(params);
		
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", jobParams);
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);

		final OAIHarvestConfiguration updatedOaiConfig = configDao.get(configId);

		// is granularity correctly read from Identify?
		Assert.assertEquals(updatedOaiConfig.getGranularity(), OAIGranularity.SECOND);
	}
	
	@Test 
	public void testHarvestFromDate() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecords2.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21&from=2015-01-01T01%3A00%3A00Z")).andReturn(response1);
		replay(httpClient);
		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(new Date(1420070400000L)));
			
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		dbUnitHelper.dump("/tmp/asdf.xml");
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
		expect(httpClient.executeGet("http://oai.medvik.cz/medvik2cpk/oai?verb=ListRecords&metadataPrefix=marc21&from=2015-01-01")).andReturn(response1);
		replay(httpClient);

		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(301L));
		params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(new Date(1420070400000L)));
			
		Long jobExecutionId = jobExecutor.execute("oaiHarvestJob", new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
	}
	
	@Test
	public void testJobParamsTransformation() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecordsFrom.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21&from=2015-01-02T01%3A00%3A00Z")).andReturn(response1);
		replay(httpClient);
		
		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(new Date(1420156800000L)));
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		reset(httpClient);
		response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		response1 = this.getClass().getResourceAsStream("/sample/ListRecordsFrom.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21&from=2015-01-03T01%3A00%3A00Z")).andReturn(response1);
		replay(httpClient);

		params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter("300"));
		params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter("2015-01-03T01:00:00Z"));
		jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST, new JobParameters(params));
		exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);	
	}
	
	@Test
	public void testDeleteRecord() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/IdentifyNonstandardGranularity.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecordsNLKdeleted1.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/ListRecordsNLKdeleted2.xml");
		expect(httpClient.executeGet("http://oai.medvik.cz/medvik2cpk/oai?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://oai.medvik.cz/medvik2cpk/oai?verb=ListRecords&metadataPrefix=marc21")).andReturn(response1);
		expect(httpClient.executeGet("http://oai.medvik.cz/medvik2cpk/oai?verb=ListRecords&resumptionToken=xaiutmvy00003")).andReturn(response2);
		replay(httpClient);
		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		final Long confID = 301L;
		params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		OAIHarvestConfiguration config = configDao.get(confID);
		HarvestedRecord record = recordDao.findByIdAndHarvestConfiguration("oai:medvik.cz:111111", config);
		Assert.assertNotNull(record, "Record not stored.");
		Assert.assertNotNull(record.getDeleted());
	}
	
	@Test
	public void testHarvestSpecificSet() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/IdentifyMLP.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecordsMLPset.xml");
		expect(httpClient.executeGet("http://web2.mlp.cz/cgi/oai?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://web2.mlp.cz/cgi/oai?verb=ListRecords&metadataPrefix=marc21&set=complete")).andReturn(response1);
		replay(httpClient);
		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(303L));
		
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
	}
	
	@Test
	public void testHarvestOneByOne() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListIdentifiersMZK.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/GetRecordMZK1.xml");
		InputStream response3 = this.getClass().getResourceAsStream("/sample/GetRecordMZK2.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListIdentifiers&metadataPrefix=marc21")).andReturn(response1);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=GetRecord&metadataPrefix=marc21&identifier=oai%3Aaleph.mzk.cz%3AMZK01-100000151")).andReturn(response2);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=GetRecord&metadataPrefix=marc21&identifier=oai%3Aaleph.mzk.cz%3AMZK01-100000152")).andReturn(response3);
		replay(httpClient);
		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_ONE_BY_ONE, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		Assert.assertNotNull(recordDao.findByIdAndHarvestConfiguration("oai:aleph.mzk.cz:MZK01-100000151", 300L));
		Assert.assertNotNull(recordDao.findByIdAndHarvestConfiguration("oai:aleph.mzk.cz:MZK01-100000152", 300L));
		
	}
}
