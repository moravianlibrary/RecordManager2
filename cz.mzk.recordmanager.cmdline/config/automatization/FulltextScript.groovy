import cz.mzk.recordmanager.server.facade.HarvestingFacade
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure
import cz.mzk.recordmanager.server.model.HarvestFrequency
import cz.mzk.recordmanager.server.model.KrameriusConfiguration
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

public class FulltextScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.FulltextScript");

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;

	@Override
	public void run() {
		List<KrameriusConfiguration> confs = krameriusConfigurationDAO.findAll()
		Collections.sort(confs, { KrameriusConfiguration a, KrameriusConfiguration b -> a.getImportConfId().compareTo(b.getImportConfId()) })
		confs.each { conf ->
			if (conf.fulltextHarvestFrequency == HarvestFrequency.DAILY) {
				try {
					harvestingFacade.incrementalFulltextJob(conf)
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Incremental fulltext harvest of %s failed", conf), jfe);
				}
			}
		}
	}

}
