package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public interface HarvestedRecordDAO extends DomainDAO<Long, HarvestedRecord> {
	
	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId, OAIHarvestConfiguration configuration);
	public HarvestedRecord findByUniqueId(String uniqueId);

}
