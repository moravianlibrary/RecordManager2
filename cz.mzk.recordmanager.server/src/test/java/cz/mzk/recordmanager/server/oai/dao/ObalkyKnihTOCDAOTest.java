package cz.mzk.recordmanager.server.oai.dao;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import cz.mzk.recordmanager.api.model.query.LogicalOperator;
import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
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
		Assert.assertEquals(tocs.size(), 3);
	}

	@Test
	public void queryByIsbn() {
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setIsbn(9788021007543L);
		List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.query(query);
		Assert.assertEquals(tocs.size(), 1);
		Assert.assertEquals(tocs.get(0).getBookId(), (Long) 3L);
	}

	@Test
	public void queryByIsbns() {
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setIsbns(Arrays.asList(9788074700781L, 1234567890123L));
		List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.query(query);
		Assert.assertEquals(tocs.size(), 1);
		Assert.assertEquals(tocs.get(0).getBookId(), (Long) 1L);
	}

	@Test
	public void queryByNbn() {
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setNbn("cnb000081537");
		List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.query(query);
		Assert.assertEquals(tocs.size(), 1);
		Assert.assertEquals(tocs.get(0).getBookId(), (Long) 3L);
	}

	@Test
	public void queryByNbnOrIsbn() {
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setLogicalOperator(LogicalOperator.OR);
		query.setNbn("cnb000081537");
		query.setIsbn(9788085622270L);
		List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.query(query);
		Assert.assertEquals(tocs.size(), 2);
	}

	@Test
	public void queryByNbnAndIsbn() {
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setLogicalOperator(LogicalOperator.AND);
		query.setNbn("cnb000081537");
		query.setIsbn(9788085622270L);
		List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.query(query);
		Assert.assertEquals(tocs.size(), 0);
	}

}
