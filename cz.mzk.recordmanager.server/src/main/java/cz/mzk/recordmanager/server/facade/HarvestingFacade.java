package cz.mzk.recordmanager.server.facade;

import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public interface HarvestingFacade {

	public void harvest(OAIHarvestConfiguration conf);

}
