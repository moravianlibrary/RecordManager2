package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.Format;
import cz.mzk.recordmanager.server.oai.dao.FormatDAO;

@Component
public class FormatDAOHibernate extends
		AbstractDomainDAOHibernate<String, Format> implements FormatDAO {

}
