package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextMonography;

public interface FulltextMonographyDAO extends DomainDAO<Long, FulltextMonography> {

	public long getFullTextSize(DedupRecord record);

	public List<String> getFullText(DedupRecord record);

}
