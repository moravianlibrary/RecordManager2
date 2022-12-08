package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordInspiration;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordInspirationDAO;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class HarvestedRecordInspirationDAOHibernate extends AbstractDomainDAOHibernate<Long, HarvestedRecordInspiration>
		implements HarvestedRecordInspirationDAO {

	@Autowired
	private HarvestedRecordDAO hrDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<HarvestedRecordInspiration> findByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecordInspiration>) session
				.createQuery(
						"from HarvestedRecordInspiration where name = :name")
				.setParameter("name", name)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HarvestedRecord> fingHrByInspiraion(String name) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr "
						+ "WHERE hr.id in (SELECT harvestedRecordId FROM HarvestedRecordInspiration WHERE name = :name)")
				.setParameter("name", name)
				.list();
	}

	@Override
	public HarvestedRecordInspiration findByHrIdAndName(Long id, String name) {
		Session session = sessionFactory.getCurrentSession();
		return (HarvestedRecordInspiration) session
				.createQuery(
						"FROM HarvestedRecordInspiration WHERE harvestedRecordId = :hrId AND name = :name")
				.setParameter("hrId", id)
				.setParameter("name", name)
				.uniqueResult();
	}

	@Override
	public void updateOrCreate(String prefix, String recordId, Inspiration inspiration) {
		HarvestedRecord hr = hrDao.find(prefix, recordId);
		if (hr == null) return;
		HarvestedRecordInspiration harvestedRecordInspiration = find(hr, inspiration);
		if (harvestedRecordInspiration == null) {
			saveOrUpdate(HarvestedRecordInspiration.create(hr, inspiration));
		} else {
			harvestedRecordInspiration.setLastHarvest(new Date());
			saveOrUpdate(harvestedRecordInspiration);
		}
	}

	@Override
	public HarvestedRecordInspiration find(HarvestedRecord hr, Inspiration inspiration) {
		Session session = sessionFactory.getCurrentSession();
		return (HarvestedRecordInspiration) session
				.createQuery(
						"FROM HarvestedRecordInspiration WHERE harvestedRecordId = :hrId AND inspirationId = :nameId")
				.setParameter("hrId", hr.getId())
				.setParameter("nameId", inspiration.getId())
				.uniqueResult();
	}

}
