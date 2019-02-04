package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.Ean;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.EanDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class EanDAOHibernate extends AbstractDomainDAOHibernate<Long, Ean>
		implements EanDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> findHrByEan(String ean) {

		try {
			Long eanLong = Long.parseLong(ean);
			Session session = sessionFactory.getCurrentSession();
			return (List<HarvestedRecord>) session
					.createQuery(
							"FROM HarvestedRecord hr "
									+ "WHERE hr.id in (SELECT harvestedRecordId FROM Ean WHERE ean = ?)")
					.setParameter(0, eanLong)
					.list();
		} catch (NumberFormatException ignore) {
		}
		return Collections.emptyList();
	}
}
