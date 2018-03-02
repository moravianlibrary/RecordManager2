package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextKramerius;

public interface FulltextKrameriusDAO extends DomainDAO<Long, FulltextKramerius> {

	long getFullTextSize(DedupRecord record);

	List<String> getFullText(DedupRecord record);

	int deleteFulltext(long hr_id);

}
