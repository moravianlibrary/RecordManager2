package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;

public interface AuthorityRecordDAO extends DomainDAO<Long, AuthorityRecord> {
	public AuthorityRecord findByIdAndHarvestConfiguration(String recordId, ImportConfiguration configuration);
	public List<AuthorityRecord> findByDedupRecord(DedupRecord dedupRecord);
}
