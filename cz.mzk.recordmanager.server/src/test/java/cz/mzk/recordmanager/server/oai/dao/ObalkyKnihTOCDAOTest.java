package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;

public class ObalkyKnihTOCDAOTest extends AbstractTest {

	@Autowired
	private ObalkyKnihTOCDAO obalkyKnihTOCDao;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/IndexRecordsToSolrJobTest.xml");
	}

	@Test
	public void findAll() {
		List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.findAll();
		Assert.assertEquals(tocs.size(), 1);
		ObalkyKnihTOC toc = tocs.get(0);
		Assert.assertEquals(toc.getBookId(), (Long) 60L);
		Assert.assertEquals(toc.getNbn(), "NBN-123");
		Assert.assertEquals(toc.getToc(), "Bozena Nemcova Babicka");
	}

}
