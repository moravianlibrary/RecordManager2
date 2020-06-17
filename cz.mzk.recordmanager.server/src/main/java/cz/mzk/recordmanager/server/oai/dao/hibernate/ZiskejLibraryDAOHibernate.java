package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.ZiskejLibrary;
import cz.mzk.recordmanager.server.oai.dao.ZiskejLibraryDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component
public class ZiskejLibraryDAOHibernate extends AbstractDomainDAOHibernate<Long, ZiskejLibrary>
		implements ZiskejLibraryDAO {

	@Override
	public ZiskejLibrary getBySigla(String sigla) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(ZiskejLibrary.class);
		crit.add(Restrictions.eq("sigla", sigla));
		return (ZiskejLibrary) crit.uniqueResult();
	}
}
