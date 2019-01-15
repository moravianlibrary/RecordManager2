import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

import cz.mzk.recordmanager.server.facade.HarvestingFacade;
import cz.mzk.recordmanager.server.facade.ImportRecordFacade;
import cz.mzk.recordmanager.server.facade.IndexingFacade;
import cz.mzk.recordmanager.server.facade.DedupFacade;
import cz.mzk.recordmanager.server.facade.ZakonyProLidiFacade;
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure;
import cz.mzk.recordmanager.server.model.HarvestFrequency;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class DailyScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.DailyScript");

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private IndexingFacade indexingFacade;

	@Autowired
	private DedupFacade dedupFacade;
	
	@Autowired
	private ImportRecordFacade importFacade;

	@Autowired
	private ZakonyProLidiFacade zakonyFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;
	
	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;
	
	@Autowired
	private DownloadImportConfigurationDAO downloadImportConfigurationDAO;

	@Override
	public void run() {
		harvestingFacade.incrementalObalkyKnihAnnotationsJob();
		oaiHarvestConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.WEEKLY) {
				try {
					harvestingFacade.incrementalHarvest(conf)
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Incremental harvest of %s failed", conf), jfe);
				}
			}
		}
		krameriusConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.WEEKLY) {
				try {
					harvestingFacade.incrementalHarvest(conf)
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Incremental harvest of %s failed", conf), jfe);
				}
			}
		}
		downloadImportConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.WEEKLY) {
				try {
					importFacade.importFactory(conf)
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Incremental harvest of %s failed", conf), jfe);
				}
			}
		}
		zakonyFacade.runZakonyProLidiHarvestJob();
		zakonyFacade.runZakonyProLidiFulltextJob();
	}

}
