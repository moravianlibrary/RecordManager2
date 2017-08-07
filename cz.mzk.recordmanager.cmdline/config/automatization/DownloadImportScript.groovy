import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.ImportRecordFacade;
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure;
import cz.mzk.recordmanager.server.model.HarvestFrequency;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;
import cz.mzk.recordmanager.server.oai.dao.DomainDAO;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;

public class DownloadImportScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.cmdline.automatization.DailyScript");

	@Autowired
	private DownloadImportConfigurationDAO downloadImportConfigurationDAO;

	@Autowired
	private ImportRecordFacade importFacade;

	@Override
	public void run() {
		downloadImportConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.DAILY) {
				try {
					importFacade.importFactory(conf)
				} catch (JobExecutionFailure jfe) {
					logger.error(String.format("Incremental harvest of %s failed", conf), jfe);
				}
			}
		}
	}

}
