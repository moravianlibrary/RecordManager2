import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.HarvestingFacade;
import cz.mzk.recordmanager.server.model.HarvestFrequency;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class DailyHarvest implements Runnable {

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;

	@Override
	public void run() {
		oaiHarvestConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.DAILY) {
				harvestingFacade.incrementalHarvest(conf)
			}
		}
	}

}
