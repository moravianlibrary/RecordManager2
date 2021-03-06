package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KramAvailabilityDAOHibernate extends AbstractDomainDAOHibernate<Long, KramAvailability>
		implements KramAvailabilityDAO {

	@Override
	public KramAvailability getByConfigAndUuid(final ImportConfiguration config, final String uuid) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(KramAvailability.class);
		crit.add(Restrictions.eq("harvestedFrom", config));
		crit.add(Restrictions.eq("uuid", uuid));
		return (KramAvailability) crit.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<KramAvailability> getByUuid(final String uuid) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(KramAvailability.class);
		crit.add(Restrictions.eq("uuid", uuid));
		return (List<KramAvailability>) crit.list();
	}
}
