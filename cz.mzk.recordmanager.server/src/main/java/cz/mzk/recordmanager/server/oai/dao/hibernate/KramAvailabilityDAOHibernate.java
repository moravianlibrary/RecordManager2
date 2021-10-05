package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.model.KramDnntLabel;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

	@Override
	@SuppressWarnings("unchecked")
	public List<KramAvailability> getByArticleKey(String articleKey) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(KramAvailability.class);
		crit.add(Restrictions.eq("articleKey", articleKey));
		return (List<KramAvailability>) crit.list();
	}

	@Override
	public void dropKeys(KramAvailability availability) {
		if (availability == null || availability.getId() == null) {
			return;
		}
		Session session = sessionFactory.getCurrentSession();
		// don't delete keys for not managed entities
		if (!session.contains(availability)) {
			return;
		}
		availability.setPage(null);
		availability.setIssue(null);
		availability.setVolume(null);
		availability.setYaer(null);
		availability.setIssn(null);
		availability.setArticleKey(null);
		availability.setType(null);
		availability.setParentUuid(null);
		availability.setDnnt(false);

		List<KramDnntLabel> labels = availability.getDnntLabels();
		availability.setDnntLabels(new ArrayList<>());
		for (KramDnntLabel label : labels) {
			session.delete(label);
		}
		session.update(availability);
		session.flush();
	}

}
