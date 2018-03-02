package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;

public interface AuthorityRecordDAO extends DomainDAO<Long, AuthorityRecord> {
	AuthorityRecord findByIdAndHarvestConfiguration(String recordId, ImportConfiguration configuration);
	AuthorityRecord findByAuthKey(String AuthKey);
}
