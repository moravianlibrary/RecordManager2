package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilar;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerSimilarDAO;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BiblioLinkerSimilarDAOHibernate extends AbstractDomainDAOHibernate<Long, BiblioLinkerSimilar>
		implements BiblioLinkerSimilarDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<BiblioLinkerSimilar> getByBilioLinkerId(Long blId, int limit) {
		Session session = sessionFactory.getCurrentSession();
		return (List<BiblioLinkerSimilar>) session
				.createQuery("from BiblioLinkerSimilar where harvested_record_id in (" +
						"select id from HarvestedRecord where biblio_linker_id = :blId)")
				.setParameter("blId", blId)
				.setMaxResults(limit)
				.list();
	}
}
