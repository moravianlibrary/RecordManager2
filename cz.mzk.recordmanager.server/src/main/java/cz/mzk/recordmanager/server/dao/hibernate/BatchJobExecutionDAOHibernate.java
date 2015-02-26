package cz.mzk.recordmanager.server.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.api.model.batch.JobExecutionQueryDTO;
import cz.mzk.recordmanager.server.dao.batch.BatchJobExecutionDAO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;
import cz.mzk.recordmanager.server.oai.dao.hibernate.AbstractDomainDAOHibernate;

@Component
public class BatchJobExecutionDAOHibernate extends
		AbstractDomainDAOHibernate<Long, BatchJobExecution> implements
		BatchJobExecutionDAO {

	@Override
	public List<BatchJobExecution> getExecutions(JobExecutionQueryDTO query) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(BatchJobExecution.class);
		if (query.getJobName() != null) {
			criteria.createAlias("jobInstance", "jobInstance");
			criteria.add(Restrictions.eq("jobInstance.name", query.getJobName()));
		}
		if (query.getStartedFrom() != null) {
			criteria.add(Restrictions.gt("start", query.getStartedFrom()));
		}
		if (query.getStartedTo() != null) {
			criteria.add(Restrictions.lt("start", query.getStartedTo()));
		}
		if (query.getLimit() > 0) {
			criteria.setMaxResults(query.getLimit());
		}
		if (query.getOffset() > 0) {
			criteria.setFirstResult(query.getOffset());
		}
		@SuppressWarnings("unchecked")
		List<BatchJobExecution> results = (List<BatchJobExecution>) criteria.list();
		return results;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<BatchJobExecution> getRunningExecutions() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(BatchJobExecution.class);
		criteria.add(Restrictions.isNull("end"));
		criteria.add(Restrictions.eq("status", "STARTED"));
		return (List<BatchJobExecution>) criteria.list();
	}

}
