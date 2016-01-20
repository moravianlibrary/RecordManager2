package automatization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.HarvestingFacade
import cz.mzk.recordmanager.server.kramerius.fulltext.KrameriusFulltexterSolr;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO

public class Harvest implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.Harvest");

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;

	public void run() {
		oaiHarvestConfigurationDAO.findAll().each({
			logger.info("About to harvest {}", it.url);
		});
	}

}
