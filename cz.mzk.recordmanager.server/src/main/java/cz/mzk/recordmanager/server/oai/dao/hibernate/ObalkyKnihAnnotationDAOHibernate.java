package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.api.model.query.LogicalOperator;
import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnnotationDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObalkyKnihAnnotationDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ObalkyKnihAnnotation> implements
		ObalkyKnihAnnotationDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihAnnotation> findByExample(ObalkyKnihAnnotation example, boolean includeNullProperties, String... excludeProperties) {
		Example exam = Example.create(example);
		if (includeNullProperties) {
			exam.setPropertySelector(Example.AllPropertySelector.INSTANCE);
		}
		if (excludeProperties != null) {
			for (String excludeProperty : excludeProperties) {
				exam.excludeProperty(excludeProperty);
			}
		}
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ObalkyKnihAnnotation.class);
		return (List<ObalkyKnihAnnotation>) crit.add(exam).list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ObalkyKnihAnnotation> findByIdentifiers(ObalkyKnihTOCQuery query) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ObalkyKnihAnnotation.class);
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
		return (List<ObalkyKnihAnnotation>) crit.list();
	}
}
