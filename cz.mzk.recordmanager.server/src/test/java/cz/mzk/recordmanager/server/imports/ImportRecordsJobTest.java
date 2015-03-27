package cz.mzk.recordmanager.server.imports;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.DBUnitHelper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

public class ImportRecordsJobTest extends AbstractTest {
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private 
	HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private DBUnitHelper dbUnitHelper;

	private String testFileXML1;
	private String testFileXML2;
	private String testFileISO1;
	private String testFileISO2;
	
	@BeforeClass
	public void init() {
		//setup files
		testFileISO1 = this.getClass().getResource("/import/iso2709/MZK01-000000146.mrc").getFile();
		testFileISO2 = this.getClass().getResource("/import/iso2709/MZK-records.mrc").getFile();
		testFileXML1 = this.getClass().getResource("/import/marcxml/NLK-192047.xml").getFile();
		testFileXML2 = this.getClass().getResource("/import/marcxml/KKFB-records.xml").getFile();
	}
	
	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}
	
	@Test
	public void testSimpleImportISO2709() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileISO1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("iso"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		HarvestedRecord insertedRecord =  harvestedRecordDao.findByUniqueId("000000146");
		Assert.assertNotNull(insertedRecord);
	}
	
	@Test
	public void testSimpleImportMarcXML() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileXML1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("xml"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		HarvestedRecord insertedRecord =  harvestedRecordDao.findByUniqueId("19790455");
		Assert.assertNotNull(insertedRecord);
	}
	
	@Test
	public void testMultileImportMarcXML() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileXML2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("xml"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("kpw0120405"));
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("kpw0120531"));
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("kpw0120435"));
	}
	
	@Test
	public void testMultileImportMarcISO() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileISO2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("iso"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("000000117"));
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("000000121"));
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("000000120"));
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("000000130"));
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("000000132"));
		Assert.assertNotNull(harvestedRecordDao.findByUniqueId("000000134"));
	}
}
