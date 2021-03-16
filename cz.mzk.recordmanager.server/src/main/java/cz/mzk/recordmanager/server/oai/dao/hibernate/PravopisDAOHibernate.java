package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.Pravopis;
import cz.mzk.recordmanager.server.oai.dao.PravopisDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component
public class PravopisDAOHibernate extends AbstractDomainDAOHibernate<Long, Pravopis> implements PravopisDAO {

	@Override
	public Pravopis findByKey(String key) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Pravopis.class);
		crit.add(Restrictions.eq("key", key));
		crit.setMaxResults(1);
		return (Pravopis) crit.uniqueResult();
	}
}
