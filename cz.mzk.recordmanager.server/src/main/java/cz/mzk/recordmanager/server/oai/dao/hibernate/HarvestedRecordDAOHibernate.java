package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
public class HarvestedRecordDAOHibernate extends
		AbstractDomainDAOHibernate<Long, HarvestedRecord> implements
		HarvestedRecordDAO {

}
