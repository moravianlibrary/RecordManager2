package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.AuthorityRecordDAO;

@Component
public class AuthorityRecordDAOHibernate extends
		AbstractDomainDAOHibernate<Long, AuthorityRecord> implements
		AuthorityRecordDAO {

	@Override
	public AuthorityRecord findByIdAndHarvestConfiguration(String recordId,
			ImportConfiguration configuration) {
		Session session = sessionFactory.getCurrentSession();
		return (AuthorityRecord) session
				.createQuery(
						"FROM AuthorityRecord WHERE oaiRecordId = ? and harvestedFrom = ?")
				.setParameter(0, recordId)
				.setParameter(1, configuration)
				.uniqueResult();
	}

	@Override
	public List<AuthorityRecord> findByDedupRecord(DedupRecord dedupRecord) {
		Session session = sessionFactory.getCurrentSession();
		
		@SuppressWarnings("unchecked")
		List<BigDecimal>  authIds = (List<BigDecimal>) session
				.createSQLQuery(
						"SELECT distinct ar.id "
						+ "FROM harvested_record hr "
						+ "INNER join authority_record ar "
						+ "ON hr.author_auth_key = ar.authority_code "
						+ "WHERE hr.dedup_record_id = ?")
				.setParameter(0, dedupRecord.getId())
				.setMaxResults(1)
				.list();
		
		List<AuthorityRecord> result = new ArrayList<>();
		for (BigDecimal authId: authIds) {
			result.add(get(authId.longValue()));
		}
		return result;
	}

	@Override
	public AuthorityRecord findByAuthKey(String AuthKey) {
		Session session = sessionFactory.getCurrentSession();
		
		return (AuthorityRecord) session
				.createQuery(
						"FROM AuthorityRecord WHERE authorityCode = ?")
				.setParameter(0, AuthKey)
				.uniqueResult();
	}
}
