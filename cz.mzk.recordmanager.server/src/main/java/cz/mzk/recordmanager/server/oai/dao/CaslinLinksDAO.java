package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.CaslinLinks;

public interface CaslinLinksDAO extends DomainDAO<Long, CaslinLinks> {

	CaslinLinks getBySigla(String sigla);

}
