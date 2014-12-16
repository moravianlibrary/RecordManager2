package cz.mzk.recordmanager.server.oai.dao.hibernate;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.ContactPerson;
import cz.mzk.recordmanager.server.oai.dao.ContactPersonDAO;

@Component
public class ContactPersonDAOHibernate extends
		AbstractDomainDAOHibernate<Long, ContactPerson> implements
		ContactPersonDAO {

}
