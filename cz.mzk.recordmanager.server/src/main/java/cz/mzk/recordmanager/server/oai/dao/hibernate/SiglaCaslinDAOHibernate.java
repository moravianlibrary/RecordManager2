package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.SiglaCaslin;
import cz.mzk.recordmanager.server.oai.dao.SiglaCaslinDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component
public class SiglaCaslinDAOHibernate extends AbstractDomainDAOHibernate<Long, SiglaCaslin> implements SiglaCaslinDAO {

	@Override
	public SiglaCaslin getBySigla(String sigla) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(SiglaCaslin.class);
		crit.add(Restrictions.eq("sigla", sigla));
		return (SiglaCaslin) crit.uniqueResult();
	}

}
