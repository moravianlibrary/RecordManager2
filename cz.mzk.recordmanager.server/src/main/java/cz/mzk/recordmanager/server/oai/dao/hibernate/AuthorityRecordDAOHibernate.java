package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.AuthorityRecordDAO;

@Component
public class AuthorityRecordDAOHibernate extends
		AbstractDomainDAOHibernate<Long, AuthorityRecord> implements
		AuthorityRecordDAO {

	@Override
	public AuthorityRecord findByIdAndHarvestConfiguration(String recordId,
			OAIHarvestConfiguration configuration) {
		Session session = sessionFactory.getCurrentSession();
		return (AuthorityRecord) session
				.createQuery(
						"FROM AuthorityRecord WHERE oaiRecordId = ? and harvestedFrom = ?")
				.setParameter(0, recordId)
				.setParameter(1, configuration)
				.uniqueResult();
	}
}
