package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public interface FulltextKrameriusDAO extends DomainDAO<Long, FulltextKramerius> {

	long getFullTextSize(DedupRecord record);

	List<String> getFullText(DedupRecord record);

	int deleteFulltext(long hr_id);

	String getPolicy(Long harvested_record_id);

	boolean isDeduplicatedFulltext(HarvestedRecord record, List<Long> importConfIdForDedup);

}
