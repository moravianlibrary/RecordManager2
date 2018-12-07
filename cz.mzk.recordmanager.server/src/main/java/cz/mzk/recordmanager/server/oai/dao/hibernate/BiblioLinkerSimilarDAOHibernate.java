package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimiliar;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerSimilarDAO;
import org.springframework.stereotype.Component;

@Component
public class BiblioLinkerSimilarDAOHibernate extends AbstractDomainDAOHibernate<Long, BiblioLinkerSimiliar>
		implements BiblioLinkerSimilarDAO {

}
