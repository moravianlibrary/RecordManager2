package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.DBUnitHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.DedupRecord;

public class DedupRecordDAOTest extends AbstractTest {

	@Autowired
	private DedupRecordDAO dedupRecordDao;

	@Autowired
	private DBUnitHelper dbUnitHelper;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/DedupRecordLocator.xml");
	}

	@Test
	public void test() {
		DedupRecord record = new DedupRecord();
		dedupRecordDao.persist(record);
	}

}
