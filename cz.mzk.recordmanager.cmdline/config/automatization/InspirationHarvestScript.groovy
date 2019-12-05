import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.ImportRecordFacade;
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure;

public class InspirationHarvestScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.InspirationHarvestScript");

	@Autowired
	private ImportRecordFacade importFacade;

	@Override
	public void run() {
		try {
			importFacade.harvestInspirationsJob();
		} catch (JobExecutionFailure jfe) {
			logger.error(String.format("Harvest of inspirations failed"), jfe);
		}
	}

}
