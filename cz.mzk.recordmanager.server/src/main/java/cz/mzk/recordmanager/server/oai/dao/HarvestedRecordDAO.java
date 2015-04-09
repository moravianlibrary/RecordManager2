package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public interface HarvestedRecordDAO extends DomainDAO<HarvestedRecordId, HarvestedRecord> {

	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId, OAIHarvestConfiguration configuration);

	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId, Long configurationId);

	@Deprecated
	public HarvestedRecord findByRecordId(String oaiRecordId);

	public List<HarvestedRecord> getByDedupRecord(DedupRecord dedupRecord);

}
