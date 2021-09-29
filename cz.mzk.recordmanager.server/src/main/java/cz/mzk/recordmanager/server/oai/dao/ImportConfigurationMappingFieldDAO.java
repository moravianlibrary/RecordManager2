package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.ImportConfigurationMappingField;

import java.util.List;

public interface ImportConfigurationMappingFieldDAO extends DomainDAO<Long, ImportConfigurationMappingField> {

	List<ImportConfigurationMappingField> findByParentImportConf(long parentImportConf);

}
