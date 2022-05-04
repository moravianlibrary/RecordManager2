package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;
import cz.mzk.recordmanager.server.oai.dao.LocDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LocDAOHibernate extends AbstractDomainDAOHibernate<Long, Loc>
		implements LocDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> findHrByLoc(String loc) {
		Session session = sessionFactory.getCurrentSession();
		if (loc == null) return Collections.emptyList();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr WHERE uniqueId.harvestedFromId=100001 AND deleted IS NULL" +
								" AND hr.id in (SELECT harvestedRecordId FROM Loc WHERE loc = :loc " +
								"AND subfield = :subfield)")
				.setParameter("loc", loc)
				.setParameter("subfield", "a")
				.list();
	}

	@Override
	public List<HarvestedRecord> findHrByLoc(List<String> loc) {
		List<HarvestedRecord> results = new ArrayList<>();
		for (String id : loc) {
			results.addAll(findHrByLoc(id));
		}
		return results;
	}

}
