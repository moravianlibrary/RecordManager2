package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.Library;
import cz.mzk.recordmanager.server.oai.dao.LibraryDAO;

@Component
public class LibraryDAOHibernate extends
		AbstractDomainDAOHibernate<Long, Library> implements LibraryDAO {

}
