import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.HarvestingFacade;
import cz.mzk.recordmanager.server.facade.IndexingFacade;
import cz.mzk.recordmanager.server.facade.DedupFacade;
import cz.mzk.recordmanager.server.model.HarvestFrequency;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class DailyScriptExample implements Runnable {

	private static final String SOLR_URL = "http://localhost:8983/solr/biblio";

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
		oaiHarvestConfigurationDAO.findAll().each { conf ->
			if (conf.harvestFrequency == HarvestFrequency.DAILY) {
				harvestingFacade.incrementalHarvest(conf)
			}
		}
		dedupFacade.deduplicate();
		indexingFacade.index(SOLR_URL);
	}

}
