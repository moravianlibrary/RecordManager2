package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.SiglaAll;

import java.util.List;
import java.util.Set;

public interface SiglaAllDAO extends DomainDAO<Long, SiglaAll> {

	List<SiglaAll> findSigla(String sigla);

	List<SiglaAll> findSiglaByImportConfId(Long id);

	Set<String> getParticipatingSigla(String type);

	boolean isParticipating(String sigla, String type);

	String getIdPrefix(String sigla);

}
