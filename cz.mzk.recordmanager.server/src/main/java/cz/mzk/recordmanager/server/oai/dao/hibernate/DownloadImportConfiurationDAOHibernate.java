package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;

@Component
public class DownloadImportConfiurationDAOHibernate extends
		AbstractDomainDAOHibernate<Long, DownloadImportConfiguration> implements
		DownloadImportConfigurationDAO {

}
