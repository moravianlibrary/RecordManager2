package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.model.SkatKey.SkatKeyCompositeId;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;

@Component
public class SkatKeyDAOHibernate extends AbstractDomainDAOHibernate<SkatKeyCompositeId,SkatKey> implements SkatKeyDAO{

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public List<SkatKey> getSkatKeysForRecord(Long skatRecordId) {
		Session session = sessionFactory.getCurrentSession();
		return (List<SkatKey>)
				session.createSQLQuery("SELECT skat_record_id,sigla,local_record_id,manually_merged"
						+ " FROM skat_keys"
						+ " WHERE skat_record_id = ?")
						.setResultTransformer(new ResultTransformer() {
							
							@Override
							public Object transformTuple(Object[] tuple, String[] aliases) {
								SkatKeyCompositeId compositeId = new SkatKeyCompositeId();
								boolean manuallyMerged = false;
								for (int i = 0; i < tuple.length; i++) {
									switch (aliases[i]) {
									case "skat_record_id": compositeId.setSkatHarvestedRecordId(((BigDecimal)tuple[i]).longValue()); break;
									case "sigla": compositeId.setSigla(((String)tuple[i])); break;
									case "local_record_id": compositeId.setRecordId((String)tuple[i]); break;
									case "manually_merged": manuallyMerged = (boolean)tuple[i]; break;
									}
								}
								SkatKey key = new SkatKey(compositeId);
								key.setManuallyMerged(manuallyMerged);
								return key;
							}
							
							@SuppressWarnings("rawtypes")
							@Override
							public List transformList(List collection) {
								return collection;
							}
						})
						.setParameter(0, skatRecordId)
						.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SkatKey> findSkatKeysBySkatId(Long skatId) {
		Session session = sessionFactory.getCurrentSession();
		return (List<SkatKey>) session
				.createQuery("FROM SkatKey WHERE skat_record_id = ?")
				.setParameter(0, skatId).list();
	}

}
