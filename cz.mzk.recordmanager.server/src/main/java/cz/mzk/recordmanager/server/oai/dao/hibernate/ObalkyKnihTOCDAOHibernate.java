package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.api.model.query.LogicalOperator;
import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihTOCDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObalkyKnihTOCDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ObalkyKnihTOC> implements
		ObalkyKnihTOCDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihTOC> findByExample(ObalkyKnihTOC example, boolean includeNullProperties, String... excludeProperties) {
		Example exam = Example.create(example);
		if (includeNullProperties) {
			exam.setPropertySelector(Example.AllPropertySelector.INSTANCE);
		}
		if (excludeProperties != null) {
			for (String excludeProperty : excludeProperties) {
				exam.excludeProperty(excludeProperty);
			}
		}
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ObalkyKnihTOC.class);
		return (List<ObalkyKnihTOC>) crit.add(exam).list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihTOC> findByIsbn(Long isbn) {
		return sessionFactory.getCurrentSession().createQuery("from ObalkyKnihTOC where bibInfo.isbn = :isbn")
				.setParameter("isbn", isbn).list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihTOC> query(ObalkyKnihTOCQuery query) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ObalkyKnihTOC.class);
		Junction oper = (query.getLogicalOperator() == LogicalOperator.AND) ? Restrictions.conjunction() : Restrictions.disjunction();
		crit.add(oper);
		if (query.getEans() != null && !query.getEans().isEmpty()) {
			oper.add(Restrictions.in("bibInfo.ean", query.getEans()));
		}
		if (query.getIsbns() != null && !query.getIsbns().isEmpty()) {
			oper.add(Restrictions.in("bibInfo.isbn", query.getIsbns()));
		}
		if (query.getOclcs() != null && !query.getOclcs().isEmpty()) {
			oper.add(Restrictions.in("bibInfo.oclc", query.getOclcs()));
		}
		if (query.getNbns() != null && !query.getNbns().isEmpty()) {
			oper.add(Restrictions.in("bibInfo.nbn", query.getNbns()));
		}
		return (List<ObalkyKnihTOC>) crit.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihTOC> findByBookId(Long book_id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(ObalkyKnihTOC.class);
		crit.add(Restrictions.eq("bookId", book_id));
		return crit.list();
	}
}
