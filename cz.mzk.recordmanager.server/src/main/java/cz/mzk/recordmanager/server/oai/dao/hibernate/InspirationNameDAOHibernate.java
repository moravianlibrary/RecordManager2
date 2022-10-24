package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationType;
import cz.mzk.recordmanager.server.model.InspirationName;
import cz.mzk.recordmanager.server.oai.dao.InspirationNameDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Component
public class InspirationNameDAOHibernate extends AbstractDomainDAOHibernate<Long, InspirationName>
		implements InspirationNameDAO {


	@Override
	public InspirationName getOrCreate(String name, InspirationType type) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<InspirationName> cq = cb.createQuery(InspirationName.class);
		Root<InspirationName> root = cq.from(InspirationName.class);
		Predicate restrictions = cb.and(
				cb.equal(root.get("name"), name),
				cb.equal(root.get("type"), type));
		cq.select(root).where(restrictions);
		TypedQuery<InspirationName> typedQuery = session.createQuery(cq);
		try {
			return typedQuery.getSingleResult();
		} catch (NoResultException ex) {
			return saveOrUpdate(InspirationName.create(name, type));
		}
	}

}
