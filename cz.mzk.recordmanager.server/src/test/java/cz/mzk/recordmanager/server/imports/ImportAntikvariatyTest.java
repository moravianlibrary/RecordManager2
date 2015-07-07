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
import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.AntikvariatyRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;

public class ImportAntikvariatyTest extends AbstractTest {
	
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private DownloadImportConfigurationDAO configDao;	
	
	@Autowired 
	private AntikvariatyRecordDAO antikDao;
	
	@Autowired
	private HttpClient httpClient;
	
	
	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}
	
	@Test
	public void simpleTest() throws Exception {
		
		final Long confId = 500L;
		DownloadImportConfiguration config = configDao.get(confId);
		Assert.assertNotNull(config);
		Assert.assertNotNull(config.getUrl());
		
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/antikvariaty/antikvariaty-base.xml");
		expect(httpClient.executeGet("http://local.antikvariaty.mzk/file.xml")).andReturn(response0);
		replay(httpClient);
		
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT_ANTIKVARIATY);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confId));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		
		AntikvariatyRecord toBeChangedRecord = antikDao.get(1L);
		Assert.assertNotNull(toBeChangedRecord);
		Assert.assertTrue(toBeChangedRecord.getCatalogueIds().size() == 1);
		Assert.assertEquals(toBeChangedRecord.getCatalogueIds().get(0),"zb00026");
		
		
		reset(httpClient);
		response0 = this.getClass().getResourceAsStream("/antikvariaty/antikvariaty-update.xml");
		expect(httpClient.executeGet("http://local.antikvariaty.mzk/file.xml")).andReturn(response0);
		replay(httpClient);
		
		job = jobRegistry.getJob(Constants.JOB_ID_IMPORT_ANTIKVARIATY);
		params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confId));
		params.put(Constants.JOB_PARAM_REPEAT, new JobParameter(1L));
		jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		
		AntikvariatyRecord updatedRecord = antikDao.get(1L);
		Assert.assertNotNull(updatedRecord);
		Assert.assertTrue(updatedRecord.getCatalogueIds().size() == 1);
		Assert.assertEquals(updatedRecord.getCatalogueIds().get(0),"xxxxxxxxxxxxxxx");
		Assert.assertTrue(updatedRecord.getUpdated().compareTo(toBeChangedRecord.getUpdated()) > 0);
		
		
	}

}
