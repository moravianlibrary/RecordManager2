package cz.mzk.recordmanager.server.oai.harvest;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.InputStream;
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
import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.AuthorityRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;

public class OAIHarvestAuthorityJobTest extends AbstractTest{
	
	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private AuthorityRecordDAO authorityRecordDao;
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestedConfigurationDao;
	
	private ImportConfiguration oaiConfiguration;
	
	@BeforeMethod
	public void initLocator() throws Exception {
		dbUnitHelper.init("dbunit/ImportAuthorityRecords.xml");
		oaiConfiguration = oaiHarvestedConfigurationDao.get(400L);
	}
	
	@Test
	public void testHarvestAuthorityRecords() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/auth/Identify.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/auth/ListRecords1.xml");
		expect(httpClient.executeGet("http://aleph.nkp.cz/OAI?verb=Identify")).andReturn(response0);
		expect(httpClient.executeGet("http://aleph.nkp.cz/OAI?verb=ListRecords&metadataPrefix=marc21&set=AUTH")).andReturn(response1);
		replay(httpClient);
		
		final Long configId = oaiConfiguration.getId();
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("configurationId", new JobParameter(configId));
		JobParameters jobParams = new JobParameters(params);
		
		Long jobExecutionId = jobExecutor.execute(Constants.JOB_ID_HARVEST_AUTH, jobParams);
		JobExecution exec = jobExplorer.getJobExecution(jobExecutionId);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		AuthorityRecord authRec = authorityRecordDao.findByIdAndHarvestConfiguration("oai:aleph-nkp.cz:AUT10-000000001", oaiConfiguration);
		Assert.assertNotNull(authRec);
		Assert.assertEquals(authRec.getAuthorityCode(), "jk01010001");
		authRec = authorityRecordDao.findByIdAndHarvestConfiguration("oai:aleph-nkp.cz:AUT10-000000006", oaiConfiguration);
		Assert.assertEquals(authRec.getAuthorityCode(), "jk01010009");
		Assert.assertNotNull(authRec);
		authRec = authorityRecordDao.findByIdAndHarvestConfiguration("oai:aleph-nkp.cz:AUT10-000000007", oaiConfiguration);
		Assert.assertEquals(authRec.getAuthorityCode(), "jk01010010");
		Assert.assertNotNull(authRec);
	}
}
