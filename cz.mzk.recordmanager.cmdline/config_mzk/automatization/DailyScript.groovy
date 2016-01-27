import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.joda.time.LocalDate;

import cz.mzk.recordmanager.server.facade.HarvestingFacade;
import cz.mzk.recordmanager.server.facade.HarvestingFacadeImpl;
import cz.mzk.recordmanager.server.facade.IndexingFacade;
import cz.mzk.recordmanager.server.facade.DedupFacade;
import cz.mzk.recordmanager.server.model.HarvestFrequency;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class DailyScriptExample implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization");

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private IndexingFacade indexingFacade;

	@Autowired
	private DedupFacade dedupFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;

	@Override
	public void run() {
		// Obalky knih harvest (new table of content is published every 3rd day of the month)
		LocalDate now = new LocalDate();
		if (now.getDayOfMonth() > 3) {
			LocalDate lastHarvest = new LocalDate(harvestingFacade.getLastObalkyKnihHarvest());
			if (lastHarvest.getMonthOfYear() != now.getMonthOfYear()) {
				harvestingFacade.obalkyKnihHarvest();
			}
		}

		oaiHarvestConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.DAILY) {
				harvestingFacade.incrementalHarvest(conf)
			}
		}
		indexingFacade.indexHarvestedRecords();
	}

}
