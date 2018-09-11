package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.oai.dao.OclcDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OclcDAOHibernate extends AbstractDomainDAOHibernate<Long, Oclc>
		implements OclcDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> findHrByOclc(String oclc) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr "
								+ "WHERE hr.id in (SELECT harvestedRecordId FROM Oclc WHERE oclc = ?)")
				.setParameter(0, oclc)
				.list();
	}
}
