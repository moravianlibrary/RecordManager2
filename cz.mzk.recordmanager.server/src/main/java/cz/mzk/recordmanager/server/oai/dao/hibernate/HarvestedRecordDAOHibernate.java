package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
public class HarvestedRecordDAOHibernate extends
		AbstractDomainDAOHibernate<Long, HarvestedRecord> implements
		HarvestedRecordDAO {

	@Override
	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId,
			OAIHarvestConfiguration configuration) {
		Session session = sessionFactory.getCurrentSession();
		return (HarvestedRecord) session
				.createQuery(
						"from HarvestedRecord where recordId = ? and harvestedFrom = ?")
				.setParameter(0, recordId).setParameter(1, configuration)
				.uniqueResult();
	}

	@Override
	public HarvestedRecord findByRecordId(String uniqueId) {
		Session session = sessionFactory.getCurrentSession();
		return (HarvestedRecord) session
				.createQuery("from HarvestedRecord where record_id = ?")
				.setParameter(0, uniqueId)
				.uniqueResult();
	}

}
