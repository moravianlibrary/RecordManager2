package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;

@Component
public class Cosmotron996DAOHibernate extends AbstractDomainDAOHibernate<Long, Cosmotron996>
	implements Cosmotron996DAO{

	@Override
	public Cosmotron996 findByIdAndHarvestConfiguration(String recordId,
			ImportConfiguration configuration) {
		Session session = sessionFactory.getCurrentSession();
		return (Cosmotron996) session
				.createQuery(
						"from Cosmotron996 where recordId = ? and harvestedFrom = ?")
				.setParameter(0, recordId).setParameter(1, configuration.getId())
				.uniqueResult();
	}

	@Override
	public Cosmotron996 findByIdAndHarvestConfiguration(String recordId,
			Long configurationId) {
		Session session = sessionFactory.getCurrentSession();
		return (Cosmotron996) session
				.createQuery(
						"from Cosmotron996 where recordId = ? and harvestedFrom = ?")
				.setParameter(0, recordId).setParameter(1, configurationId)
				.uniqueResult();
		
	}
}
