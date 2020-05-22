package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KrameriusConfiurationDAOHibernate extends
		AbstractDomainDAOHibernate<Long, KrameriusConfiguration> implements
		KrameriusConfigurationDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<KrameriusConfiguration> getAllWithoutOaiConfigs() {
		Session session = sessionFactory.getCurrentSession();
		return (List<KrameriusConfiguration>) session
				.createQuery(
						"FROM KrameriusConfiguration kc "
								+ "WHERE kc.id not in (SELECT id FROM OAIHarvestConfiguration)")
				.list();
	}
}
