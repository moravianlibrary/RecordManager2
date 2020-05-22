import cz.mzk.recordmanager.server.facade.HarvestingFacade
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure
import cz.mzk.recordmanager.server.model.HarvestFrequency
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class KramAvailabilityScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.KramAvailabilityScript")

	@Autowired
	private HarvestingFacade harvestingFacade

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO

	@Override
	void run() {
		krameriusConfigurationDAO.findAll().each { conf ->
			if (conf.availabilityHarvestFrequency == HarvestFrequency.DAILY) {
				try {
					harvestingFacade.fullKramAvailability(conf)
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Incremental availability harvest of %s failed", conf), jfe)
				}
			}
		}
	}

}
