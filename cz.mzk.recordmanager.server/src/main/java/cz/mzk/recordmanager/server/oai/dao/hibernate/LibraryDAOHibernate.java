package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.api.model.ContactPersonDto;
import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.OaiHarvestConfigurationDto;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.Library;
import cz.mzk.recordmanager.server.oai.dao.LibraryDAO;

import java.util.ArrayList;
import java.util.List;

@Component
public class LibraryDAOHibernate extends
		AbstractDomainDAOHibernate<Long, Library> implements LibraryDAO {

	@Override
	public List<OAIHarvestConfiguration> getOAIHarvestConfigurations(Long libraryId) {
		Session session = sessionFactory.getCurrentSession();

		Criteria libraryCrit = session.createCriteria(ImportConfiguration.class, "impComfig");
		libraryCrit.createAlias("impComfig.library", "lib");
		libraryCrit.add(Restrictions.eq("lib.id", libraryId));
		List<OAIHarvestConfiguration> configs = libraryCrit.list();

		return configs;

	}

	@Override
	public void updateLibrary(Library library) {
		sessionFactory.getCurrentSession().update(library);
	}

	@Override
	public void updateOaiHarvestConfiguration(OAIHarvestConfiguration config) {
		sessionFactory.getCurrentSession().update(config);
	}

}
