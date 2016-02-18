package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextKramerius;

public interface FulltextKrameriusDAO extends DomainDAO<Long, FulltextKramerius> {

	public long getFullTextSize(DedupRecord record);

	public List<String> getFullText(DedupRecord record);

}
