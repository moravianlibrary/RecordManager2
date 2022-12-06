package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationType;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;

@Component
public class InspirationDAOHibernate extends AbstractDomainDAOHibernate<Long, Inspiration>
		implements InspirationDAO {

	private static final int NAME_LENGHT = 128;

	@Override
	public Inspiration getOrCreate(String name, InspirationType type) throws IOException {
		if (NAME_LENGHT < name.length()) throw new IOException(String.format("Name '%s' is too long!", name));
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Inspiration> cq = cb.createQuery(Inspiration.class);
		Root<Inspiration> root = cq.from(Inspiration.class);
		Predicate restrictions = cb.and(
				cb.equal(root.get("name"), name),
				cb.equal(root.get("type"), type));
		cq.select(root).where(restrictions);
		TypedQuery<Inspiration> typedQuery = session.createQuery(cq);
		try {
			return typedQuery.getSingleResult();
		} catch (NoResultException ex) {
			return saveOrUpdate(Inspiration.create(name, type));
		}
	}

}
