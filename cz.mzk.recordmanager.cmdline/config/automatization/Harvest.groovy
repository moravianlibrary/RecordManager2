import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.HarvestingFacade;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class Harvest implements Runnable {

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;

	@Override
	public void run() {
		oaiHarvestConfigurationDAO.findAll().each { conf ->
			harvestingFacade.incrementalHarvest(conf)
		}
	}

}