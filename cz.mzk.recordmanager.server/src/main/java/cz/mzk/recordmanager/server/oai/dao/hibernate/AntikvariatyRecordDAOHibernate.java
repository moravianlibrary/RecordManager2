package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.AntikvariatyRecordDAO;

@Component
public class AntikvariatyRecordDAOHibernate extends AbstractDomainDAOHibernate<Long, AntikvariatyRecord>
		implements AntikvariatyRecordDAO {

	@Override
	public String getLinkToAntikvariaty(DedupRecord dr) {
		Session session = sessionFactory.getCurrentSession();
		return (String) session
				.createSQLQuery(
						"select url from antikvariaty_url_view v where v.dedup_record_id = :dedupRecordId")
				.setParameter("dedupRecordId", dr.getId())
				.setMaxResults(1)
				.uniqueResult();
	}

}
