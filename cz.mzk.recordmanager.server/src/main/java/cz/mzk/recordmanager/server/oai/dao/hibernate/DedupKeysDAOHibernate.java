package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupKeys;
import cz.mzk.recordmanager.server.oai.dao.DedupKeysDAO;

@Component
public class DedupKeysDAOHibernate extends
		AbstractDomainDAOHibernate<Long, DedupKeys> implements DedupKeysDAO {

}
