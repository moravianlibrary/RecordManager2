package cz.mzk.recordmanager.server.facade;

import java.util.Date;

import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public interface HarvestingFacade {

	public void incrementalHarvest(OAIHarvestConfiguration conf);

	public void fullHarvest(OAIHarvestConfiguration conf);

	public Date getLastHarvest(OAIHarvestConfiguration conf);

}
