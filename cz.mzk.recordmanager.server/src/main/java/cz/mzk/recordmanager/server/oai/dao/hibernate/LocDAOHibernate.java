package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;
import cz.mzk.recordmanager.server.oai.dao.LocDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocDAOHibernate extends AbstractDomainDAOHibernate<Long, Loc>
		implements LocDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> findHrByLoc(String loc) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr WHERE hr.id in (SELECT harvestedRecordId FROM Loc WHERE loc = :loc)")
				.setParameter("loc", loc)
				.list();
	}

}
