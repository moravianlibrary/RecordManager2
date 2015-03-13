package cz.mzk.recordmanager.server.oai.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.DedupRecord;

public class DedupRecordDAOTest extends AbstractTest {

	@Autowired
	private DedupRecordDAO dedupRecordDao;

	@Test
	public void test() {
		DedupRecord record = new DedupRecord();
		dedupRecordDao.persist(record);
	}

}
