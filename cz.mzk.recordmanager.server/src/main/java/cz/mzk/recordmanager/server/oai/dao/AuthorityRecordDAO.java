package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public interface AuthorityRecordDAO extends DomainDAO<Long, AuthorityRecord> {
	public AuthorityRecord findByIdAndHarvestConfiguration(String recordId, OAIHarvestConfiguration configuration);
}
