package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;
import cz.mzk.recordmanager.server.oai.dao.LocDAO;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cz.mzk.recordmanager.server.metadata.institutions.MarcitLocMetadataMarcRecord.CLEAN_LOC;
import static cz.mzk.recordmanager.server.metadata.institutions.MarcitLocMetadataMarcRecord.LOC_FOR_SEARCH;

@Component
public class LocDAOHibernate extends AbstractDomainDAOHibernate<Long, Loc>
		implements LocDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> findHrByLoc(final String loc, final String subfield) {
		Session session = sessionFactory.getCurrentSession();
		if (loc == null) return Collections.emptyList();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr WHERE uniqueId.harvestedFromId=100001 AND deleted IS NULL" +
								" AND hr.id in (SELECT harvestedRecordId FROM Loc WHERE loc = :loc " +
								"AND subfield = :subfield)")
				.setParameter("loc", loc)
				.setParameter("subfield", subfield)
				.list();
	}

	@Override
	public List<HarvestedRecord> findHrByLoc(final List<String> loc) {
		List<HarvestedRecord> results = new ArrayList<>();
		for (String id : loc) {
			List<HarvestedRecord> subResults = findHrByLoc(id, "a");
			if (!subResults.isEmpty()) {
				results.addAll(subResults);
				continue;
			}
			results.addAll(findHrByLoc(CleaningUtils.replaceAll(id, CLEAN_LOC, ""), LOC_FOR_SEARCH));
		}
		return results;
	}

}
