package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimiliar;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerSimilarDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BiblioLinkerSimilarDAOHibernate extends AbstractDomainDAOHibernate<Long, BiblioLinkerSimiliar>
		implements BiblioLinkerSimilarDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<BiblioLinkerSimiliar> getByBilioLinkerId(Long blId, int limit) {
		Session session = sessionFactory.getCurrentSession();
		return (List<BiblioLinkerSimiliar>) session
				.createQuery("from BiblioLinkerSimiliar where harvested_record_id in (" +
						"select id from HarvestedRecord where biblio_linker_id = ?)")
				.setParameter(0, blId)
				.setMaxResults(limit)
				.list();
	}
}
