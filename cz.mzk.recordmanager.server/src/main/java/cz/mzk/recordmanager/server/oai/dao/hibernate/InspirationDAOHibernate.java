package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;

@Component
public class InspirationDAOHibernate extends AbstractDomainDAOHibernate<Long, Inspiration>
	implements InspirationDAO{

}
