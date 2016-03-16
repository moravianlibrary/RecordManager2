package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.DedupRecord;

public class FulltextKrameriusDAOTest extends AbstractTest {

	@Autowired
	private DedupRecordDAO dedupRecordDao;

	@Autowired
	private FulltextKrameriusDAO fulltextKrameriusDao;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/IndexRecordsToSolrJobTest.xml");
	}

	@Test
	public void getFullText() {
		DedupRecord record = dedupRecordDao.get(100L);
		List<String> fulltext = fulltextKrameriusDao.getFullText(record);
		Assert.assertEquals(fulltext.size(), 1);
		Assert.assertEquals(fulltext.get(0), "test indexace fulltextu");
	}

}
