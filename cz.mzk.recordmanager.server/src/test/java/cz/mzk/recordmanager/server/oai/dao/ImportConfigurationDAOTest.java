package cz.mzk.recordmanager.server.oai.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestFrequency;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public class ImportConfigurationDAOTest extends AbstractTest {

	@Autowired
	private ImportConfigurationDAO importConfigurationDao;

	@Autowired
	private LibraryDAO libraryDao;

	@Autowired
	private ContactPersonDAO contactPersonDao;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}

	@Test
	public void getMZK() {
		ImportConfiguration importConf = importConfigurationDao.get(300L);
		Assert.assertNotNull(importConf);
		Assert.assertEquals(importConf.getHarvestFrequency(), HarvestFrequency.DAILY);
	}

	@Test
	public void getNLK() {
		ImportConfiguration importConf = importConfigurationDao.get(301L);
		Assert.assertNotNull(importConf);
		Assert.assertEquals(importConf.getHarvestFrequency(), HarvestFrequency.UNSPECIFIED);
	}

	@Test
	public void persist() {
		OAIHarvestConfiguration importConf = new OAIHarvestConfiguration();
		importConf.setLibrary(libraryDao.get(100L));
		importConf.setContact(contactPersonDao.get(200L));
		importConf.setUrl("http://aleph.mzk.cz/OAI");
		importConf.setHarvestFrequency(HarvestFrequency.WEEKLY);
		importConfigurationDao.persist(importConf);
		ImportConfiguration importConfFromDB = importConfigurationDao.get(importConf.getId());
		Assert.assertNotNull(importConfFromDB);
		Assert.assertEquals(importConfFromDB.getHarvestFrequency(), HarvestFrequency.WEEKLY);
	}

}
