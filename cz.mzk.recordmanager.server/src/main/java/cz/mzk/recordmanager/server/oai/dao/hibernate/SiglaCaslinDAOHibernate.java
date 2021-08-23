package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.SiglaCaslin;
import cz.mzk.recordmanager.server.oai.dao.SiglaCaslinDAO;
import org.springframework.stereotype.Component;

@Component
public class SiglaCaslinDAOHibernate extends AbstractDomainDAOHibernate<Long, SiglaCaslin> implements SiglaCaslinDAO {

}
