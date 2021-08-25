package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.CaslinLinks;
import cz.mzk.recordmanager.server.oai.dao.CaslinLinksDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component
public class CaslinLinksDAOHibernate extends AbstractDomainDAOHibernate<Long, CaslinLinks> implements CaslinLinksDAO {

	@Override
	public CaslinLinks getBySigla(String sigla) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(CaslinLinks.class);
		crit.add(Restrictions.eq("sigla", sigla));
		return (CaslinLinks) crit.uniqueResult();
	}

}
