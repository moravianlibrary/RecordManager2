package cz.mzk.recordmanager.server.miscellaneous;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

public class FilterCaslinRecordsTest extends AbstractTest{

	@Autowired
	private HarvestedRecordDAO hrDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@BeforeMethod
	public void init() throws Exception{
		dbUnitHelper.init("dbunit/CaslinFilterRecords.xml");
	}
	
	@Test
	public void CaslinFilterTest() throws Exception{
		Job job = jobRegistry.getJob(Constants.JOB_ID_FILTER_CASLIN);
		JobParameters jobParams = new JobParameters();
		jobLauncher.run(job, jobParams);
		
		HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration("MZK01-000000135", 316L);
		MarcRecord marcRecord = marcXmlParser.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));

		Assert.assertEquals(marcRecord.getDataFields("996"), Collections.emptyList());
		Assert.assertNotNull(hr.getDeleted());
	}
}
