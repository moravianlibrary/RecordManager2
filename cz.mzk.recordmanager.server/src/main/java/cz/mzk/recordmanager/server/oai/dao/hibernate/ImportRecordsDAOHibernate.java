package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;

@Component
public class ImportRecordsDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ImportConfiguration> implements
		ImportConfigurationDAO {

	@Override
	public ImportConfiguration findByIdPrefix(String prefix) {
		Session session = sessionFactory.getCurrentSession();
		return (ImportConfiguration) session
				.createQuery(
						"from ImportConfiguration where idPrefix = ?")
				.setParameter(0, prefix)
				.uniqueResult();
	}

}
