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
		record.setIsbn("978-80-904832-7-9");
		record.setTitle("test");
		dedupRecordDao.persist(record);
	}

}
