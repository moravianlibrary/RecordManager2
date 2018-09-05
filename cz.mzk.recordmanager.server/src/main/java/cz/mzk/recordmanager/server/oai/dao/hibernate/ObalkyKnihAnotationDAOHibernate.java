package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnotationDAO;
import org.springframework.stereotype.Component;

@Component
public class ObalkyKnihAnotationDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ObalkyKnihAnotation> implements
		ObalkyKnihAnotationDAO {

}
