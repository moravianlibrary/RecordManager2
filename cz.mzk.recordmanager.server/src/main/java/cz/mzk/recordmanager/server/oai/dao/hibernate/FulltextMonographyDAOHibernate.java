package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.oai.dao.FulltextMonographyDAO;

@Component
public class FulltextMonographyDAOHibernate extends AbstractDomainDAOHibernate<Long, FulltextMonography> implements
FulltextMonographyDAO {

	
}
