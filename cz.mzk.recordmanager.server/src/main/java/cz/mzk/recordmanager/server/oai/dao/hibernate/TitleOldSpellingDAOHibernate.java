package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.TitleOldSpelling;
import cz.mzk.recordmanager.server.oai.dao.TitleOldSpellingDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component
public class TitleOldSpellingDAOHibernate extends AbstractDomainDAOHibernate<Long, TitleOldSpelling> implements TitleOldSpellingDAO {

	@Override
	public TitleOldSpelling findByKey(String key) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(TitleOldSpelling.class);
		crit.add(Restrictions.eq("key", key));
		crit.setMaxResults(1);
		return (TitleOldSpelling) crit.uniqueResult();
	}
}
