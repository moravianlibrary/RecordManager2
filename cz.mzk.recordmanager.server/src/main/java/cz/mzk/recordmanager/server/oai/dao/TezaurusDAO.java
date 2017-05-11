package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.TezaurusRecord;

public interface TezaurusDAO extends DomainDAO<Long, TezaurusRecord> {

	public TezaurusRecord findByIdAndHarvestConfiguration(String recordId,
			ImportConfiguration configuration);

	public TezaurusRecord findByConfigAndSourceFieldAndName(
			ImportConfiguration configuration, String sourceField, String name);

}
