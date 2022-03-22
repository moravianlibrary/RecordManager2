package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.Sigla;
import cz.mzk.recordmanager.server.oai.dao.SiglaDAO;

@Component
public class SiglaDAOHibernate extends AbstractDomainDAOHibernate<Long, Sigla> 
	implements SiglaDAO{

	@SuppressWarnings("unchecked")
	@Override
	public List<Sigla> findSiglaByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Sigla>) session
				.createQuery(
						"from Sigla where sigla = :sigla")
				.setParameter("sigla", name)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Sigla> findSiglaByImportConfId(Long id) {
		Session session = sessionFactory.getCurrentSession();
		return (List<Sigla>) session
				.createQuery("from Sigla where import_conf_id = :importConfId")
				.setParameter("importConfId", id).list();
	}

}
