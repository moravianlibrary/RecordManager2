package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.model.SkatKey.SkatKeyCompositeId;

public interface SkatKeyDAO extends DomainDAO<SkatKeyCompositeId, SkatKey> {

	List<SkatKey> getSkatKeysForRecord(Long skatRecordId);

	List<SkatKey> findSkatKeysBySkatId(Long skatId);

}
