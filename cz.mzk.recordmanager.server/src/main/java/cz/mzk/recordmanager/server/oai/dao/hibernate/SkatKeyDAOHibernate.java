package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.model.SkatKey.SkatKeyCompositeId;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;

@Component
public class SkatKeyDAOHibernate extends AbstractDomainDAOHibernate<SkatKeyCompositeId,SkatKey> implements SkatKeyDAO{

}
