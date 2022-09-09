package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.oai.dao.IsbnDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IsbnDAOHibernate extends AbstractDomainDAOHibernate<Long, Isbn>
		implements IsbnDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<HarvestedRecord> findHrByIsbn(Long isbn) {
		Session session = sessionFactory.getCurrentSession();
		return (List<HarvestedRecord>) session
				.createQuery(
						"FROM HarvestedRecord hr "
								+ "WHERE hr.id in (SELECT harvestedRecordId FROM Isbn WHERE isbn = :isbn)")
				.setParameter("isbn", isbn)
				.list();
	}

}
