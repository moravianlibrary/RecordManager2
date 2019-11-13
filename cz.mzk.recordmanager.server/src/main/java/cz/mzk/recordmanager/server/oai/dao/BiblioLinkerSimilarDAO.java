package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilar;

import java.util.List;

public interface BiblioLinkerSimilarDAO extends DomainDAO<Long, BiblioLinkerSimilar> {

	List<BiblioLinkerSimilar> getByBilioLinkerId(Long blId, int limit);
}
