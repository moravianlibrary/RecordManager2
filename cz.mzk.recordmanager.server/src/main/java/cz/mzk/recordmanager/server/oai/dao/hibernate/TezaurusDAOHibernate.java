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
						"from TezaurusRecord where recordId = ? and harvestedFrom = ?")
				.setParameter(0, recordId).setParameter(1, configuration)
				.uniqueResult();
	}

}
