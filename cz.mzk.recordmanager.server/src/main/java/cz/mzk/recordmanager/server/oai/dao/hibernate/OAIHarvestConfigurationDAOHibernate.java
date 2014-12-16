package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

@Component
public class OAIHarvestConfigurationDAOHibernate extends
		AbstractDomainDAOHibernate<Long, OAIHarvestConfiguration> implements
		OAIHarvestConfigurationDAO {

}
