package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.ZiskejLibrary;
import cz.mzk.recordmanager.server.oai.dao.ZiskejLibraryDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Component
public class ZiskejLibraryDAOHibernate extends AbstractDomainDAOHibernate<Long, ZiskejLibrary>
		implements ZiskejLibraryDAO {

	@Override
	public ZiskejLibrary getBySigla(String sigla) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ZiskejLibrary> cq = cb.createQuery(ZiskejLibrary.class);
		Root<ZiskejLibrary> root = cq.from(ZiskejLibrary.class);
		Predicate restrictions = cb.equal(root.get("sigla"), sigla);
		cq.select(root).where(restrictions);
		TypedQuery<ZiskejLibrary> typedQuery = session.createQuery(cq);
		try {
			return typedQuery.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}

}
