package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;

@Component
public class DedupRecordDAOHibernate extends
		AbstractDomainDAOHibernate<Long, DedupRecord> implements DedupRecordDAO {

}
