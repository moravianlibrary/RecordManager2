package cz.mzk.recordmanager.server.facade;

import java.time.LocalDateTime;

import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public interface HarvestingFacade {

	public void fullHarvest(OAIHarvestConfiguration conf);

	public LocalDateTime getLastFullHarvest(OAIHarvestConfiguration conf);

	public void incrementalHarvest(OAIHarvestConfiguration conf);

	public LocalDateTime getLastHarvest(OAIHarvestConfiguration conf);

	public void obalkyKnihHarvest();

	public LocalDateTime getLastObalkyKnihHarvest();

}
