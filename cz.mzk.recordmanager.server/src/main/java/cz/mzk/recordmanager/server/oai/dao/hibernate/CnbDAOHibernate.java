package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.CnbDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CnbDAOHibernate extends AbstractDomainDAOHibernate<Long, Cnb>
		implements CnbDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> findHrByCnb(String cnb) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr "
								+ "WHERE hr.id in (SELECT harvestedRecordId FROM Cnb WHERE cnb = :cnb)")
				.setParameter("cnb", cnb)
				.list();
	}
}
