package cz.mzk.recordmanager.server.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dao.batch.BatchJobParameterDAO;
import cz.mzk.recordmanager.server.model.batch.BatchJobParam;
import cz.mzk.recordmanager.server.model.batch.BatchJobParam.JobParamId;
import cz.mzk.recordmanager.server.oai.dao.hibernate.AbstractDomainDAOHibernate;

@Component
public class BatchJobParameterDAOHibernate extends
		AbstractDomainDAOHibernate<JobParamId, BatchJobParam> implements
		BatchJobParameterDAO {

	@SuppressWarnings("unchecked")
	public List<BatchJobParam> findByJobExecutionId(Long jobExecutionId) {
		Session session = sessionFactory.getCurrentSession();
		return (List<BatchJobParam>) session
				.createQuery("from JobParam where id.jobExecutionId = :id")
				.setParameter("id", jobExecutionId).list();
	}

}
