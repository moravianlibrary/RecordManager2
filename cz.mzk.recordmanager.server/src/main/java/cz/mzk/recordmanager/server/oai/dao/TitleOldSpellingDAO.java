package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.TitleOldSpelling;

public interface TitleOldSpellingDAO extends DomainDAO<Long, TitleOldSpelling> {

	TitleOldSpelling findByKey(String key);

}
