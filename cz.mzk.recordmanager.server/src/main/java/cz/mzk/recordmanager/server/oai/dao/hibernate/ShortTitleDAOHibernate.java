package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.ShortTitle;
import cz.mzk.recordmanager.server.oai.dao.ShortTitleDAO;

@Component
public class ShortTitleDAOHibernate extends AbstractDomainDAOHibernate<Long, ShortTitle>
		implements ShortTitleDAO {

}
