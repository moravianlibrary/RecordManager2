package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;

@Component
public class InspirationDAOHibernate extends AbstractDomainDAOHibernate<Long, Inspiration>
	implements InspirationDAO{

	@SuppressWarnings("unchecked")
	@Override
	public List<Inspiration> findByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Inspiration>) session
				.createQuery(
						"from Inspiration where name = ?")
				.setParameter(0, name)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HarvestedRecord> fingHrByInspiraion(String name) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr "
						+ "WHERE hr.id in (SELECT harvestedRecordId FROM Inspiration WHERE name = ?)")
				.setParameter(0, name)
				.list();
	}

	@Override
	public Inspiration findByHrIdAndName(Long id, String name) {
		Session session = sessionFactory.getCurrentSession();
		return (Inspiration) session
				.createQuery(
						"FROM Inspiration WHERE harvestedRecordId = ? AND name = ?)")
				.setParameter(0, id)
				.setParameter(1, name)
				.uniqueResult();
	}

}
