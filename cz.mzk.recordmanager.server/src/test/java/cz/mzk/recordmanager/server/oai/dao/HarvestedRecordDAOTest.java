package cz.mzk.recordmanager.server.oai.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class HarvestedRecordDAOTest extends AbstractTest {

	@Autowired
	private DedupRecordDAO dedupRecordDAO;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/IndexRecordsToSolrJobTest.xml");
	}

	@Test
	public void existsByDedupRecord() {
		Assert.assertTrue(harvestedRecordDao.existsByDedupRecord(dedupRecordDAO.get(60L)));
		Assert.assertFalse(harvestedRecordDao.existsByDedupRecord(dedupRecordDAO.get(101L)));
	}

	@Test
	public void findBySolrId() {
		HarvestedRecord record = harvestedRecordDao.findBySolrId("MZK.MZK01-000000117");
		Assert.assertNotNull(record);
		Assert.assertEquals(record.getUniqueId().getRecordId(), "MZK01-000000117");
		Assert.assertEquals(record.getHarvestedFrom().getIdPrefix(), "MZK");
	}

}
