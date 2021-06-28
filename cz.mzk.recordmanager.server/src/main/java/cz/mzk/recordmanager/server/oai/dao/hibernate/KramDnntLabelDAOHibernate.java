package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.KramDnntLabel;
import cz.mzk.recordmanager.server.oai.dao.KramDnntLabelDAO;
import org.springframework.stereotype.Component;

@Component
public class KramDnntLabelDAOHibernate extends AbstractDomainDAOHibernate<Long, KramDnntLabel>
		implements KramDnntLabelDAO {

}
