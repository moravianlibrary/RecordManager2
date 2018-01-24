package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;

import java.util.List;

public interface Cosmotron996DAO extends DomainDAO<Long, Cosmotron996>{

	Cosmotron996 findByIdAndHarvestConfiguration(String recordId, ImportConfiguration configuration);
	
	Cosmotron996 findByIdAndHarvestConfiguration(String recordId, Long configuration);

	List<Cosmotron996> findByParentId(Long configurationId, String parentRecordId);

	List<Cosmotron996> findByParentId(HarvestedRecordUniqueId parentUniqueId);
}
