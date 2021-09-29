package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.ImportConfigurationMappingField;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationMappingFieldDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImportConfiurationMappingFieldDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ImportConfigurationMappingField> implements
		ImportConfigurationMappingFieldDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<ImportConfigurationMappingField> findByParentImportConf(long parentImportConf) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(ImportConfigurationMappingField.class);
		crit.add(Restrictions.eq("parentImportConfId", parentImportConf));
		return (List<ImportConfigurationMappingField>) crit.list();
	}

}
