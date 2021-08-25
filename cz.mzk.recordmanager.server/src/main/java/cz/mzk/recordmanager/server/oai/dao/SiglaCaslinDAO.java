package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.SiglaCaslin;

public interface SiglaCaslinDAO extends DomainDAO<Long, SiglaCaslin> {

	SiglaCaslin getBySigla(String sigla);

}
