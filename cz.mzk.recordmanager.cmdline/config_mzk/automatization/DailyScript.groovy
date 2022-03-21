import cz.mzk.recordmanager.server.facade.HarvestingFacade
import cz.mzk.recordmanager.server.facade.IndexingFacade
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure
import cz.mzk.recordmanager.server.model.HarvestFrequency
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

public class DailyScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization");

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private IndexingFacade indexingFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;

	@Override
	public void run() {
		oaiHarvestConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.DAILY) {
				try {
					harvestingFacade.incrementalHarvest(conf)
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Incremental harvest of %s failed", conf), jfe);
				}
			}
		}
		indexingFacade.indexHarvestedRecords();
	}

}
