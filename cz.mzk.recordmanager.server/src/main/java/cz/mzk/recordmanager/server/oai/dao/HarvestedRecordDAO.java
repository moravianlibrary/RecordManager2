package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;

public interface HarvestedRecordDAO extends DomainDAO<Long, HarvestedRecord> {

	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId, ImportConfiguration configuration);

	public HarvestedRecord findByIdAndHarvestConfiguration(String recordId, Long configurationId);

	public HarvestedRecord findBySolrId(String solrId);
	
	public HarvestedRecord findByHarvestConfAndRaw001Id(Long ConfigurationId, String id);
	
	public HarvestedRecord findByHarvestConfAndTezaurus(Long configurationId, String tezaurus);

	public HarvestedRecord get(HarvestedRecordUniqueId uniqueId);

	public List<HarvestedRecord> getByDedupRecord(DedupRecord dedupRecord);
	
	public List<HarvestedRecord> getByHarvestConfiguration(ImportConfiguration configuration);

	public List<HarvestedRecord> getByDedupRecordWithDeleted(DedupRecord dedupRecord);

	public boolean existsByDedupRecord(DedupRecord dedupRecord);

	public HarvestedRecord findByRecordId(String uniqueId);
	
	public void dropDedupKeys(HarvestedRecord hr);

	public void updateTimestampOnly(HarvestedRecord hr);

	boolean existsUpvApplicationId(String applId);

	void deleteUpvApplicationRecord(String appId);

}
