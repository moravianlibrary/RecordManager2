package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.model.InspirationName;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class InspirationDAOHibernate extends AbstractDomainDAOHibernate<Long, Inspiration>
		implements InspirationDAO {

	@Autowired
	private HarvestedRecordDAO hrDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<Inspiration> findByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Inspiration>) session
				.createQuery(
						"from Inspiration where name = :name")
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
						+ "WHERE hr.id in (SELECT harvestedRecordId FROM Inspiration WHERE name = :name)")
				.setParameter("name", name)
				.list();
	}

	@Override
	public Inspiration findByHrIdAndName(Long id, String name) {
		Session session = sessionFactory.getCurrentSession();
		return (Inspiration) session
				.createQuery(
						"FROM Inspiration WHERE harvestedRecordId = :hrId AND name = :name")
				.setParameter("hrId", id)
				.setParameter("name", name)
				.uniqueResult();
	}

	@Override
	public void updateOrCreate(String prefix, String recordId, InspirationName inspirationName) {
		HarvestedRecord hr = hrDao.find(prefix, recordId);
		if (hr == null) return;
		Inspiration inspiration = find(hr, inspirationName);
		if (inspiration == null) {
			saveOrUpdate(Inspiration.create(hr, inspirationName));
		} else {
			inspiration.setLastHarvest(new Date());
			saveOrUpdate(inspiration);
		}
	}

	@Override
	public Inspiration find(HarvestedRecord hr, InspirationName inspirationName) {
		Session session = sessionFactory.getCurrentSession();
		return (Inspiration) session
				.createQuery(
						"FROM Inspiration WHERE harvestedRecordId = :hrId AND inspirationNameId = :nameId")
				.setParameter("hrId", hr.getId())
				.setParameter("nameId", inspirationName.getId())
				.uniqueResult();
	}

}
