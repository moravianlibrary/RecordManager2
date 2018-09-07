package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnotationDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Example;
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
}
