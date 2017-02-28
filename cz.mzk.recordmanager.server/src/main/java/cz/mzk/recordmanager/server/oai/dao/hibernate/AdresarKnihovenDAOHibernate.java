package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AdresarKnihoven;
import cz.mzk.recordmanager.server.oai.dao.AdresarKnihovenDAO;

@Component
public class AdresarKnihovenDAOHibernate extends AbstractDomainDAOHibernate<Long, AdresarKnihoven>
		implements AdresarKnihovenDAO {

	@Override
	public AdresarKnihoven findByRecordId(String recordId) {
		Session session = sessionFactory.getCurrentSession();
		return (AdresarKnihoven) session
				.createQuery(
						"from AdresarKnihoven where recordId = ? ")
				.setParameter(0, recordId)
				.uniqueResult();
	}

}
