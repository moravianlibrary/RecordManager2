package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
public class HarvestedRecordDAOHibernate extends
		AbstractDomainDAOHibernate<Long, HarvestedRecord> implements
		HarvestedRecordDAO {

	@Override
	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId,
			ImportConfiguration configuration) {
		Session session = sessionFactory.getCurrentSession();
		return (HarvestedRecord) session
				.createQuery(
						"from HarvestedRecord where uniqueId.recordId = ? and uniqueId.harvestedFromId = ?")
				.setParameter(0, recordId).setParameter(1, configuration.getId())
				.uniqueResult();
	}

	@Override
	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId,
			Long configurationId) {
		Session session = sessionFactory.getCurrentSession();
		return (HarvestedRecord) session
				.createQuery(
						"from HarvestedRecord where uniqueId.recordId = ? and uniqueId.harvestedFromId = ?")
				.setParameter(0, recordId).setParameter(1, configurationId)
				.uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> getByDedupRecord(DedupRecord dedupRecord) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecord>) session
				.createQuery("from HarvestedRecord where dedupRecord = ?")
				.setParameter(0, dedupRecord)
				.list();
	}

	@Override
	public HarvestedRecord get(HarvestedRecordUniqueId uniqueId) {
		return findByIdAndHarvestConfiguration(uniqueId.getRecordId(), uniqueId.getHarvestedFromId());
	}

	@Override
	public boolean existsByDedupRecord(DedupRecord dedupRecord) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(HarvestedRecord.class);
		crit.add(Restrictions.eq("dedupRecord", dedupRecord));
		crit.setProjection(Projections.id());
		crit.setMaxResults(1);
		return crit.uniqueResult() != null;
	}

}
