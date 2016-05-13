import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.time.LocalDateTime;

import cz.mzk.recordmanager.server.facade.HarvestingFacade;
import cz.mzk.recordmanager.server.facade.HarvestingFacadeImpl;
import cz.mzk.recordmanager.server.facade.IndexingFacade;
import cz.mzk.recordmanager.server.facade.DedupFacade;
import cz.mzk.recordmanager.server.facade.ImportRecordFacade;
import cz.mzk.recordmanager.server.model.HarvestFrequency;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class WeeklyScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.WeeklyScript");

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private IndexingFacade indexingFacade;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDAO;

	@Override
	public void run() {
		oaiHarvestConfigurationDAO.findAll().each { conf ->
			if (conf.url == "http://aleph.mzk.cz/OAI") {
				harvestingFacade.fullHarvest(conf);
			}
		}
		indexingFacade.reindexHarvestedRecords();
	}

}
