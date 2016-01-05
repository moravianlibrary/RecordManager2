package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Example;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihTOCDAO;

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

}
