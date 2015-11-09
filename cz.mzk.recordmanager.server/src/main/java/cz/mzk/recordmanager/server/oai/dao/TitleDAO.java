package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.dedup.clustering.TitleClusterable;
import cz.mzk.recordmanager.server.dedup.clustering.TitleForDeduplication;
import cz.mzk.recordmanager.server.model.Title;

public interface TitleDAO extends DomainDAO<Long, Title> {

	public List<TitleForDeduplication> getTitleForDeduplicationByYear(Long year, int minPages, int maxPages, String lang);
	
	public List<TitleClusterable> getPeriodicalsTitleForDeduplication(Long year);
}
