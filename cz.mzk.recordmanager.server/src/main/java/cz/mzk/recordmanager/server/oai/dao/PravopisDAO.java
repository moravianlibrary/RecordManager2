package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.Pravopis;

public interface PravopisDAO extends DomainDAO<Long, Pravopis> {

	Pravopis findByKey(String key);

}
