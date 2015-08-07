package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.dedup.TitleForDeduplication;
import cz.mzk.recordmanager.server.model.Title;

public interface TitleDAO extends DomainDAO<Long, Title> {

	public List<TitleForDeduplication> getTitleForDeduplicationByYear(Long year, int minPages, int maxPages, String lang);
}
