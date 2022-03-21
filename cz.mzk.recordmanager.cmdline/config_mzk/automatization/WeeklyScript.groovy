import cz.mzk.recordmanager.server.facade.HarvestingFacade
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDateTime

public class WeeklyScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.WeeklyScript");

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;

	@Override
	public void run() {
		oaiHarvestConfigurationDAO.findAll().each { conf ->
			if (conf.url == "http://aleph.mzk.cz/OAI") {
				try {
					harvestingFacade.fullHarvest(conf);
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Full harvest of %s failed", conf), jfe);
				}
			}
		}
		// Obalky knih harvest (new table of content is published every 3rd day of the month)
		LocalDateTime now = LocalDateTime.now();
		if (now.getDayOfMonth() > 3) {
			LocalDateTime lastHarvest = harvestingFacade.getLastObalkyKnihHarvest();
			if (lastHarvest == null || lastHarvest.getMonth() != now.getMonth()) {
				try {
					harvestingFacade.obalkyKnihHarvest();
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Harvest of obalkyknihToc failed"), jfe);
				}
			}
		}
	}

}
