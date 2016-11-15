package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;

public interface DownloadImportConfigurationDAO extends DomainDAO<Long, DownloadImportConfiguration> {
	void update(DownloadImportConfiguration configuration);

}
