package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;

@Component
public class KrameriusConfiurationDAOHibernate extends
		AbstractDomainDAOHibernate<Long, KrameriusConfiguration> implements
		KrameriusConfigurationDAO {

}
