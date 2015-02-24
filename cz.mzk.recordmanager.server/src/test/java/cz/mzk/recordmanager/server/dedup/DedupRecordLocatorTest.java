package cz.mzk.recordmanager.server.dedup;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class DedupRecordLocatorTest extends AbstractTest {
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private DedupRecordLocator dedupRecordLocator;
	
	@BeforeMethod
	public void initLocator() throws Exception {
		dbUnitHelper.init("dbunit/DedupRecordLocator.xml");
	}
	
	@Test
	public void simpleLocate() throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(100L);
		Assert.assertNotNull(record, "Harvested record not loaded.");
		DedupRecord dedupRecord = dedupRecordLocator.locate(record);
		Assert.assertNotNull(dedupRecord, "Record not located.");
		Assert.assertEquals(dedupRecord.getId(), new Long(11));
	}
	
	@Test
	public void simpleLocateNonExisting() throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(50L);
		Assert.assertNotNull(record, "Harvested record not loaded.");
		DedupRecord dedupRecord = dedupRecordLocator.locate(record);
		Assert.assertNull(dedupRecord);
	}
	
	@Test
	public void locateSimilar() throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(200L);
		Assert.assertNotNull(record, "Harvested record not loaded.");
		DedupRecord dedupRecord = dedupRecordLocator.locate(record);
		Assert.assertNotNull(dedupRecord, "Record not located.");
		Assert.assertEquals(dedupRecord.getId(), new Long(11));
		
		record = harvestedRecordDao.get(201L);
		Assert.assertNotNull(record, "Harvested record not loaded.");
		dedupRecord = dedupRecordLocator.locate(record);
		Assert.assertNotNull(dedupRecord, "Record not located.");
		Assert.assertEquals(dedupRecord.getId(), new Long(11));
		
		record = harvestedRecordDao.get(300L);
		Assert.assertNotNull(record, "Harvested record not loaded.");
		dedupRecord = dedupRecordLocator.locate(record);
		Assert.assertNotNull(dedupRecord, "Record not located.");
		Assert.assertEquals(dedupRecord.getId(), new Long(11));
	}

}
