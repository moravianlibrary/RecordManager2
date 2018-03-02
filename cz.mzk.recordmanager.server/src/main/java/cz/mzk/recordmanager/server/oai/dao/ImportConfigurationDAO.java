package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.ImportConfiguration;

public interface ImportConfigurationDAO extends DomainDAO<Long, ImportConfiguration>{

	List<ImportConfiguration> findByIdPrefix(String prefix);

}
