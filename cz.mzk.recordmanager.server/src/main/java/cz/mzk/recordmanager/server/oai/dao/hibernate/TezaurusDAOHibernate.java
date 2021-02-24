package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.TezaurusRecord;
import cz.mzk.recordmanager.server.oai.dao.TezaurusDAO;

@Component
public class TezaurusDAOHibernate extends
		AbstractDomainDAOHibernate<Long, TezaurusRecord> implements TezaurusDAO {

	@Override
	public TezaurusRecord findByIdAndHarvestConfiguration(String recordId,
			ImportConfiguration configuration) {
		Session session = sessionFactory.getCurrentSession();
		return (TezaurusRecord) session
				.createQuery(
						"from TezaurusRecord where recordId = :recordId and harvestedFrom = :harvestedFrom")
				.setParameter("recordId", recordId)
				.setParameter("harvestedFrom", configuration)
				.uniqueResult();
	}

	@Override
	public TezaurusRecord findByConfigAndSourceFieldAndName(
			ImportConfiguration configuration, String sourceField, String name) {
		Session session = sessionFactory.getCurrentSession();
		return (TezaurusRecord) session
				.createQuery(
						"FROM TezaurusRecord WHERE harvestedFrom = :harvestedFrom AND tezaurusKey.sourceField = :sourceField AND tezaurusKey.name = :name")
				.setParameter("harvestedFrom", configuration)
				.setParameter("sourceField", sourceField)
				.setParameter("name", name).uniqueResult();
	}

}
