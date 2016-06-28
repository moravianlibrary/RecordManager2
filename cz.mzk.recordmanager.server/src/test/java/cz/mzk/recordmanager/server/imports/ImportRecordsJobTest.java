package cz.mzk.recordmanager.server.imports;

import java.io.ByteArrayInputStream;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.DBUnitHelper;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

public class ImportRecordsJobTest extends AbstractTest {
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private Cosmotron996DAO cosmotron996Dao;
	
	@Autowired
	private DBUnitHelper dbUnitHelper;

	private String testFileXML1;
	private String testFileXML2;
	private String testFileISO1;
	private String testFileISO2;
	private String testFileAleph1;
	private String testFileAleph2;
	private String testFileLine1;
	private String testFileLine2;
	private String testFilePatents1;
	private String testFilePatents2;
	private String testFileOai;
	private String testFolderOai;
	private String testFileCosmotron1;
	private String testFileCosmotron2;
	private String testFileOsobnosti1;
	private String testFileOsobnosti2;
	
	@BeforeClass
	public void init() {
		//setup files
		testFileISO1 = this.getClass().getResource("/import/iso2709/MZK01-000000146.mrc").getFile();
		testFileISO2 = this.getClass().getResource("/import/iso2709/MZK-records.mrc").getFile();
		testFileXML1 = this.getClass().getResource("/import/marcxml/NLK-192047.xml").getFile();
		testFileXML2 = this.getClass().getResource("/import/marcxml/KKFB-records.xml").getFile();
		testFileAleph1 = this.getClass().getResource("/import/marcaleph/MZK01-000004171.txt").getFile();
		testFileAleph2 = this.getClass().getResource("/import/marcaleph/MZK-records.txt").getFile();
		testFileLine1 = this.getClass().getResource("/import/marcline/MZK01-000000116.mrc").getFile();
		testFileLine2 = this.getClass().getResource("/import/marcline/MZK-records.mrc").getFile();
		testFilePatents1 = this.getClass().getResource("/import/patents/201546-B6-305523.xml").getFile();
		testFilePatents2 = this.getClass().getResource("/import/patents/PatentsRecords.xml").getFile();
		testFileOai = this.getClass().getResource("/import/oai/ANL01.000000502.ANL-CPK.xml").getFile();
		testFolderOai = this.getClass().getResource("/import/oai/").getFile();
		testFileCosmotron1 = this.getClass().getResource("/import/cosmotron/record.mrc").getFile();
		testFileCosmotron2 = this.getClass().getResource("/import/cosmotron/996.mrc").getFile();
		testFileOsobnosti1 = this.getClass().getResource("/import/osobnostiregionu/Record.xml").getFile();
		testFileOsobnosti2 = this.getClass().getResource("/import/osobnostiregionu/OsobnostiRecords.xml").getFile();
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

		HarvestedRecord insertedRecord =  harvestedRecordDao.findByIdAndHarvestConfiguration("000000146", 300L);
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
		
		HarvestedRecord insertedRecord =  harvestedRecordDao.findByIdAndHarvestConfiguration("19790455", 300L);
		Assert.assertNotNull(insertedRecord);
	}
	
	@Test
	public void testSimpleImportMarcAleph() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileAleph1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("aleph"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000004171", 300L));
	}
	
	@Test
	public void testSimpleImportMarcLine() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileLine1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("line"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000116", 300L));
	}
	
	@Test
	public void testSimpleImportPatents() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFilePatents1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("patents"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration("201546-B6-305523", 300L);
		Assert.assertNotNull(hr);
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		MarcRecord mr = new MarcRecordImpl(marcXmlParser.parseUnderlyingRecord(is));
		Assert.assertFalse(mr.getDataFields("100").isEmpty());
		Assert.assertFalse(mr.getDataFields("520").isEmpty());
	}
	
	@Test
	public void testSimpleImportOsobnosti() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileOsobnosti1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("osobnosti"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration("5", 300L);
		Assert.assertNotNull(hr);
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		MarcRecord mr = new MarcRecordImpl(marcXmlParser.parseUnderlyingRecord(is));
		Assert.assertFalse(mr.getDataFields("100").isEmpty());
		Assert.assertFalse(mr.getDataFields("670").isEmpty());
		Assert.assertEquals(mr.getDataFields("856").size(), 1);
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
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("kpw0120405", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("kpw0120531", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("kpw0120435", 300L));
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
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000117", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000121", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000120", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000130", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000132", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000134", 300L));
	}
	
	@Test
	public void testMultipleImportMarcAleph() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileAleph2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("aleph"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000116", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000117", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000119", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000120", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000121", 300L));
	}
	
	@Test
	public void testMultipleImportMarcLine() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileLine2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("line"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000116", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000117", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000119", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000120", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000121", 300L));
	}
	
	@Test
	public void testMultipleImportPatents() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFilePatents2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("patents"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("201546-B6-305523", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("201546-B6-305524", 300L));
	}

	// import oai format
	@Test
	public void testSingleFileImportOai() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT_OAI);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileOai));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000502", 300L));
	}

	@Test
	public void testMultipleFilesImportOai() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT_OAI);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFolderOai));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000502", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("000000503", 300L));
	}
	
	@Test
	public void testImportCosmotron996() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileCosmotron1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("line"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		job = jobRegistry.getJob(Constants.JOB_ID_IMPORT_COSMOTRON_996);
		params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileCosmotron2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("line"));
		jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("LiUsCat_0495991", 300L));
		Assert.assertNotNull(cosmotron996Dao.findByIdAndHarvestConfiguration("LiUsCat_0002490", 300L));
		Assert.assertNull(cosmotron996Dao.findByIdAndHarvestConfiguration("LiUsCat_0002491", 300L));
	}
	
	@Test
	public void testMultipleImportOsobnosti() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileOsobnosti2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("osobnosti"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("1", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("2", 300L));
		Assert.assertNotNull(harvestedRecordDao.findByIdAndHarvestConfiguration("3", 300L));
	}
}
