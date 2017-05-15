package cz.mzk.recordmanager.server.imports;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;

public class ZakonyProLidiTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private HttpClient httpClient;

	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}

	@Test
	public void metadataHarvestTest() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/import/zakony/Records.xml");
		expect(httpClient.executeGet("http://www.zakonyprolidi.cz/api/v1/data.xml/YearDocList?apikey=test&Collection=cs&Year=1945")).andReturn(response0);
		replay(httpClient);

		Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(1945L));
		params.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(1945L));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertNotNull(hrDao.get(new HarvestedRecordUniqueId(300L, "1945-1")));
		Assert.assertNotNull(hrDao.get(new HarvestedRecordUniqueId(300L, "1945-2")));
	}

}
