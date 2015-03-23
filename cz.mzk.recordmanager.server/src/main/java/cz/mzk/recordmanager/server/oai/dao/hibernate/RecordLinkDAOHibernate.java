package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.RecordLink;
import cz.mzk.recordmanager.server.model.RecordLink.RecordLinkId;
import cz.mzk.recordmanager.server.oai.dao.RecordLinkDAO;

@Component
public class RecordLinkDAOHibernate extends
		AbstractDomainDAOHibernate<RecordLinkId, RecordLink> implements
		RecordLinkDAO {
	
	public RecordLink findByHarvestedRecord(HarvestedRecord record) {
		Session session = sessionFactory.getCurrentSession();
		return (RecordLink) session
				.createQuery(
						"from RecordLink where id.harvestedRecord = ?")
				.setParameter(0, record)
				.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> getHarvestedRecords(DedupRecord master) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(RecordLink.class);
		crit.add(Restrictions.eq("id.dedupRecord", master));
		crit.setProjection(Projections.property("id.harvestedRecord"));
		return (List<HarvestedRecord>) crit.list();
	}

}
