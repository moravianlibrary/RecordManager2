package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;

public interface HarvestedRecordDAO extends DomainDAO<Long, HarvestedRecord> {

	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId, ImportConfiguration configuration);

	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId, Long configurationId);

	public HarvestedRecord get(HarvestedRecordUniqueId uniqueId);
	
	@Deprecated
	public HarvestedRecord findByRecordId(String oaiRecordId);

	public List<HarvestedRecord> getByDedupRecord(DedupRecord dedupRecord);
	
	public List<HarvestedRecord> getByHarvestConfiguration(ImportConfiguration configuration);

}
