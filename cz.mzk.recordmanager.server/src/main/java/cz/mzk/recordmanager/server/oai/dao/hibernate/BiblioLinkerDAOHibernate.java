package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.BiblioLinker;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerDAO;
import org.springframework.stereotype.Component;

@Component
public class BiblioLinkerDAOHibernate extends
		AbstractDomainDAOHibernate<Long, BiblioLinker> implements BiblioLinkerDAO {

}
