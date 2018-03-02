package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.TezaurusRecord;

public interface TezaurusDAO extends DomainDAO<Long, TezaurusRecord> {

	TezaurusRecord findByIdAndHarvestConfiguration(String recordId,
												   ImportConfiguration configuration);

	TezaurusRecord findByConfigAndSourceFieldAndName(
			ImportConfiguration configuration, String sourceField, String name);

}
