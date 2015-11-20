package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.ImportConfiguration;

public interface Cosmotron996DAO extends DomainDAO<Long, Cosmotron996>{

	public Cosmotron996 findByIdAndHarvestConfiguration(String recordId, ImportConfiguration configuration);
}
