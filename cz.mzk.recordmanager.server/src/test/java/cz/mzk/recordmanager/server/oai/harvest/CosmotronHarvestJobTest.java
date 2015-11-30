package cz.mzk.recordmanager.server.oai.harvest;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.marc4j.marc.Record;
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
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;

public class CosmotronHarvestJobTest extends AbstractTest {
	
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
	
	@Autowired
	private Cosmotron996DAO cosmotronDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@BeforeMethod
	public void initLocator() throws Exception {
		dbUnitHelper.init("dbunit/OAIHarvestTest.xml");
	}
		
	@Test 
	public void testHarvestRecords() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/ListRecords2.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21")).andReturn(response1);
		replay(httpClient);
			
		final Long confID = 300L;		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));			
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		HarvestedRecord hr = recordDao.findByIdAndHarvestConfiguration("MZK01-000213478", confID);
		Assert.assertNotNull(hr);
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		Record record = marcXmlParser.parseUnderlyingRecord(is);
		MarcRecord marcRecord = new MarcRecordImpl(record);
		Assert.assertEquals(marcRecord.getDataFields("996").size(), 5);
	}

	@Test
	public void testDeletedExistsRecord() throws Exception {
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
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		HarvestedRecord record = recordDao.findByIdAndHarvestConfiguration("111111", confID);
		Assert.assertNotNull(record, "Record not stored.");
		Assert.assertNotNull(record.getDeleted());
	}
	
	@Test
	public void testDeletedRecord() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/cosmotron/DeletedRecord.xml");
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&metadataPrefix=oai_marcxml_cpk")).andReturn(response1);
		replay(httpClient);
		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		final Long confID = 328L;
		params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		HarvestedRecord record = recordDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"0000003", confID);
		Assert.assertNotNull(record, "Record not stored.");
		Assert.assertNotNull(record.getDeleted());
	}
	
	@Test 
	public void testNotStored() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/cosmotron/NotStored.xml");
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&metadataPrefix=oai_marcxml_cpk")).andReturn(response1);
		replay(httpClient);
			
		final Long confID = 328L;		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));			
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		Assert.assertNull(recordDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"m0000003", confID));
	}
	
	@Test 
	public void testNew996() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/cosmotron/New9961.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/cosmotron/New9962.xml");
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&metadataPrefix=oai_marcxml_cpk")).andReturn(response1);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&resumptionToken=12345")).andReturn(response2);
		
		replay(httpClient);
			
		final Long confID = 328L;		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));			
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		OAIHarvestConfiguration config = configDao.get(confID);
		HarvestedRecord hr = recordDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"m0000002", config);
		Assert.assertNotNull(hr);
		Assert.assertNotNull(cosmotronDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"0000003", config));
		
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		Record record = marcXmlParser.parseUnderlyingRecord(is);
		MarcRecord marcRecord = new MarcRecordImpl(record);
		Assert.assertEquals(marcRecord.getDataFields("996").size(), 4);
	}
	
	@Test 
	public void testDeleted996() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/cosmotron/RecordAnd996.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/cosmotron/Deleted996.xml");
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&metadataPrefix=oai_marcxml_cpk")).andReturn(response1);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&resumptionToken=123456")).andReturn(response2);
		replay(httpClient);
			
		final Long confID = 328L;		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));			
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		OAIHarvestConfiguration config = configDao.get(confID);
		Assert.assertNotNull(recordDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"m0000002", config));
		Cosmotron996 cosmo = cosmotronDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"0000003", config);
		Assert.assertNotNull(cosmo);
		Assert.assertNotNull(cosmo.getDeleted());
	}
	
	@Test 
	public void testUpdated996() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/cosmotron/RecordAnd996.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/cosmotron/Updated996.xml");
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&metadataPrefix=oai_marcxml_cpk")).andReturn(response1);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&resumptionToken=123456")).andReturn(response2);
		replay(httpClient);
			
		final Long confID = 328L;		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));			
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		OAIHarvestConfiguration config = configDao.get(confID);
		Assert.assertNotNull(recordDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"m0000002", config));
		Cosmotron996 cosmo = cosmotronDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"0000003", config);
		Assert.assertNotNull(cosmo);
		
		InputStream is = new ByteArrayInputStream(cosmo.getRawRecord());
		Record record = marcXmlParser.parseUnderlyingRecord(is);
		MarcRecord marcRecord = new MarcRecordImpl(record);
		Assert.assertNotNull(marcRecord.getDataFields("100"));
	}
	
	@Test 
	public void testCopy996ToParentRecord() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/cosmotron/RecordAnd996.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/cosmotron/Deleted996.xml");
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&metadataPrefix=oai_marcxml_cpk")).andReturn(response1);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&resumptionToken=123456")).andReturn(response2);
		replay(httpClient);
			
		final Long confID = 328L;		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));			
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		OAIHarvestConfiguration config = configDao.get(confID);
		HarvestedRecord hr = recordDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"m0000002", config);
		Assert.assertNotNull(hr);
		
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		Record record = marcXmlParser.parseUnderlyingRecord(is);
		MarcRecord marcRecord = new MarcRecordImpl(record);
		Assert.assertEquals(marcRecord.getDataFields("996").size(), 2);
	}
	
	@Test 
	public void test996BeforeRecord() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/sample/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/sample/cosmotron/996BeforeRecord.xml");
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://katalog.cbvk.cz/i2/i2.ws.oai.cls?verb=ListRecords&metadataPrefix=oai_marcxml_cpk")).andReturn(response1);
		replay(httpClient);
			
		final Long confID = 328L;		
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));			
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_COSMOTRON, new JobParameters(params));
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		OAIHarvestConfiguration config = configDao.get(confID);
		HarvestedRecord hr = recordDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"m0000002", config);
		Assert.assertNotNull(hr);
		Assert.assertNotNull(cosmotronDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"0000003", config));
		Assert.assertNotNull(cosmotronDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"0000004", config));
		Assert.assertNotNull(cosmotronDao.findByIdAndHarvestConfiguration("CbvkUsCat"+Constants.COSMOTRON_RECORD_ID_CHAR+"0000005", config));
		
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		Record record = marcXmlParser.parseUnderlyingRecord(is);
		MarcRecord marcRecord = new MarcRecordImpl(record);
		Assert.assertEquals(marcRecord.getDataFields("996").size(), 3);
	}
	
}
