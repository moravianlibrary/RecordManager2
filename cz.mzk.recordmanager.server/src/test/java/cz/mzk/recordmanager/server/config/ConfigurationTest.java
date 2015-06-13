package cz.mzk.recordmanager.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class ConfigurationTest extends AbstractTest {

	@Autowired
	private ImportConfigurationDAO importConfigDao;
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiConfigDao;
	
	@Autowired
	private KrameriusConfigurationDAO kramConfigDao;
	
	private static final Long OAI_CONF_ID  = 300L;
	private static final Long KRAM_CONF_ID = 304L;
	
	@BeforeMethod
	public void init() throws Exception {
		dbUnitHelper.init("dbunit/ConfigTest.xml");
	}
	
	@Test
	public void test() {
		OAIHarvestConfiguration oaiConf = oaiConfigDao.get(OAI_CONF_ID);
		KrameriusConfiguration kramConf = kramConfigDao.get(KRAM_CONF_ID);
		
		Assert.assertEquals(importConfigDao.get(OAI_CONF_ID), oaiConf);
		Assert.assertEquals(importConfigDao.get(KRAM_CONF_ID), kramConf);
		
		final String model = "model2";
		kramConf.setModel(model);
		kramConfigDao.persist(kramConf);
		
		Assert.assertNotNull(kramConfigDao.get(KRAM_CONF_ID));
		Assert.assertEquals(kramConfigDao.get(KRAM_CONF_ID).getModel(), model);
	}
}
