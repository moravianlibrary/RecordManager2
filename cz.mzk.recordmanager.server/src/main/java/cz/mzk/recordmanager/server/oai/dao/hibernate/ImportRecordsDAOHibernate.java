package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;

@Component
public class ImportRecordsDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ImportConfiguration> implements
		ImportConfigurationDAO {

}
