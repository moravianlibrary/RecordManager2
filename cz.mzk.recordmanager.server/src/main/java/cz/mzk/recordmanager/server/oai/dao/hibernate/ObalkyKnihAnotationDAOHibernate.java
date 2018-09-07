package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.api.model.query.LogicalOperator;
import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnotationDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObalkyKnihAnotationDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ObalkyKnihAnotation> implements
		ObalkyKnihAnotationDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihAnotation> findByExample(ObalkyKnihAnotation example, boolean includeNullProperties, String... excludeProperties) {
		Example exam = Example.create(example);
		if (includeNullProperties) {
			exam.setPropertySelector(Example.AllPropertySelector.INSTANCE);
		}
		if (excludeProperties != null) {
			for (String excludeProperty : excludeProperties) {
				exam.excludeProperty(excludeProperty);
			}
		}
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ObalkyKnihAnotation.class);
		return (List<ObalkyKnihAnotation>) crit.add(exam).list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihAnotation> findByIdentifiers(ObalkyKnihTOCQuery query) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ObalkyKnihAnotation.class);
		Junction oper = (query.getLogicalOperator() == LogicalOperator.AND) ? Restrictions.conjunction() : Restrictions.disjunction();
		crit.add(oper);
		if (query.getIsbns() != null && !query.getIsbns().isEmpty()) {
			oper.add(Restrictions.in("bibInfo.isbn", query.getIsbns()));
		}
		if (query.getOclcs() != null && !query.getOclcs().isEmpty()) {
			oper.add(Restrictions.in("bibInfo.oclc", query.getOclcs()));
		}
		if (query.getNbns() != null && !query.getNbns().isEmpty()) {
			oper.add(Restrictions.in("bibInfo.nbn", query.getNbns()));
		}
		return (List<ObalkyKnihAnotation>) crit.list();
	}
}
