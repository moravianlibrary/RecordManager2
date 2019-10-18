package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimiliar;

import java.util.List;

public interface BiblioLinkerSimilarDAO extends DomainDAO<Long, BiblioLinkerSimiliar> {

	List<BiblioLinkerSimiliar> getByBilioLinkerId(Long blId, int limit);
}
