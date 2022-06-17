package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.SiglaAll;
import cz.mzk.recordmanager.server.oai.dao.SiglaAllDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SiglaAllDAOHibernate extends AbstractDomainDAOHibernate<Long, SiglaAll>
		implements SiglaAllDAO {

	@Override
	public List<SiglaAll> findSigla(String sigla) {
		if (sigla == null || sigla.isEmpty()) return Collections.emptyList();
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SiglaAll> cq = cb.createQuery(SiglaAll.class);
		Root<SiglaAll> root = cq.from(SiglaAll.class);
		Predicate predicateSigla = cb.equal(root.get("sigla"), sigla);
		cq.select(root).where(predicateSigla);
		TypedQuery<SiglaAll> typedQuery = session.createQuery(cq);
		return typedQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SiglaAll> findSiglaByImportConfId(Long id) {
		Session session = sessionFactory.getCurrentSession();
		return (List<SiglaAll>) session
				.createQuery("from SiglaAll where harvestedFromId = ?")
				.setParameter(0, id).list();
	}

	@Override
	public Set<String> getParticipatingSigla(String type) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Object> cq = cb.createQuery();
		Root<SiglaAll> root = cq.from(SiglaAll.class);
		Predicate predicateType = cb.isTrue(root.get(type));
		cq.multiselect(root.get("sigla")).where(cb.and(predicateType));
		TypedQuery<Object> typedQuery = session.createQuery(cq);
		return typedQuery.getResultList().stream().map(o -> o.toString()).collect(Collectors.toSet());
	}

	@Override
	public boolean isParticipating(String sigla, String type) {
		if (sigla == null || sigla.isEmpty()) return false;
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SiglaAll> cq = cb.createQuery(SiglaAll.class);
		Root<SiglaAll> root = cq.from(SiglaAll.class);
		Predicate predicateSigla = cb.equal(root.get("sigla"), sigla);
		Predicate predicateType = cb.isTrue(root.get(type));
		cq.select(root).where(cb.and(predicateSigla, predicateType));
		TypedQuery<SiglaAll> typedQuery = session.createQuery(cq);
		return !typedQuery.getResultList().isEmpty();
	}

	@Override
	public String getIdPrefix(String sigla) {
		List<SiglaAll> siglas = findSigla(sigla);
		for (SiglaAll siglaAll : siglas) {
			if (siglaAll.getHarvestedFromId() != null) {
				return siglaAll.getHarvestedFrom().getIdPrefix();
			}
		}
		return null;
	}

}
