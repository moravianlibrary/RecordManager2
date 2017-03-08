package cz.mzk.recorcmanager.server.adresar;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.oai.dao.AdresarKnihovenDAO;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;

public class AdresarHarvestTest extends AbstractTest {

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private AdresarKnihovenDAO akDao;

	@Test
	public void harvestSingleRecord() throws Exception {
		reset(httpClient);
		InputStream response1 = this.getClass().getResourceAsStream("/sample/adresar/adr000000001.xml");
		expect(httpClient.executeGet("http://aleph.nkp.cz/X?op=find-doc&doc_num=000000001&base=ADR")).andReturn(response1);
		replay(httpClient);

		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_SINGLE_ID, new JobParameter("1"));
		JobParameters jobParams = new JobParameters(params);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_HARVEST_ADRESAR, jobParams);
		Assert.assertEquals(exec.getExitStatus(), ExitStatus.COMPLETED);
		
		Assert.assertNotNull(akDao.findByRecordId("000000001"));
	}

}
