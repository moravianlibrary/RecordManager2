package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.Sigla;

public interface SiglaDAO extends DomainDAO<Long, Sigla> {

	public List<Sigla> findSiglaByName(String name);

	public List<Sigla> findSiglaByImportConfId(Long id);

}
