package cz.mzk.recordmanager.server.dedup;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class HarvestedRecordDedupMatcherTest extends AbstractTest {
	
	@Autowired
	HarvestedRecordDedupMatcher recordMatcher;
	
	@Autowired
	HarvestedRecordDAO harvestedRecordDao;
	
	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/DedupRecordMatcherTest.xml");
	}
	
	@Test
	public void simpleMatcherTest() {
		HarvestedRecordId id1 = new HarvestedRecordId(300L, "MZK01-000000199");
		HarvestedRecordId id2 = new HarvestedRecordId(302L, "KN2470000010034176");
		HarvestedRecord rec1 = harvestedRecordDao.get(id1); // 100L
		HarvestedRecord rec2 = harvestedRecordDao.get(id2); // 101L
		Assert.assertNotNull(rec1);
		Assert.assertNotNull(rec2);
		Assert.assertTrue(recordMatcher.matchRecords(rec1, rec1), "Same records not matched");
		Assert.assertTrue(recordMatcher.matchRecords(rec1, rec2), "Matching failed");
	}
}
