package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;

@Component
public class ImportRecordsDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ImportConfiguration> implements
		ImportConfigurationDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<ImportConfiguration> findByIdPrefix(String prefix) {
		Session session = sessionFactory.getCurrentSession();
		return (List<ImportConfiguration>) session
				.createQuery(
						"from ImportConfiguration where idPrefix = :idPrefix")
				.setParameter("idPrefix", prefix)
				.list();
	}

}
