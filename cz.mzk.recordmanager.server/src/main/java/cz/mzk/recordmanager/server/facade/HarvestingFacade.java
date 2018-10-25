package cz.mzk.recordmanager.server.facade;

import java.time.LocalDateTime;

import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public interface HarvestingFacade {

	void fullHarvest(OAIHarvestConfiguration conf);

	void fullHarvest(KrameriusConfiguration conf);

	void fullHarvest(Long id);

	LocalDateTime getLastFullHarvest(OAIHarvestConfiguration conf);

	LocalDateTime getLastFullHarvest(KrameriusConfiguration conf);

	void incrementalHarvest(OAIHarvestConfiguration conf);

	void incrementalHarvest(KrameriusConfiguration conf);

	void incrementalHarvest(Long id);

	LocalDateTime getLastHarvest(OAIHarvestConfiguration conf);

	LocalDateTime getLastHarvest(KrameriusConfiguration conf);

	void obalkyKnihHarvest();

	LocalDateTime getLastObalkyKnihHarvest();

	void incrementalFulltextJob(KrameriusConfiguration conf);

	void incrementalFulltextJob(Long id);

	void incrementalObalkyKnihAnnotationsJob();

}
