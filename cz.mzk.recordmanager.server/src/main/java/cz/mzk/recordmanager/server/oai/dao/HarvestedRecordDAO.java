package cz.mzk.recordmanager.server.oai.dao;

import java.util.Collection;
import java.util.List;

import cz.mzk.recordmanager.server.model.BiblioLinker;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;

public interface HarvestedRecordDAO extends DomainDAO<Long, HarvestedRecord> {

	public List<HarvestedRecord> findByIdsWithDedupRecord(List<Long> ids);

	HarvestedRecord findByIdAndHarvestConfiguration(String recordId, ImportConfiguration configuration);

	HarvestedRecord findByIdAndHarvestConfiguration(String recordId, Long configurationId);

	HarvestedRecord findBySolrId(String solrId);

	HarvestedRecord findByHarvestConfAndRaw001Id(Long ConfigurationId, String id);

	HarvestedRecord findByHarvestConfAndTezaurus(Long configurationId, String tezaurus);

	HarvestedRecord get(HarvestedRecordUniqueId uniqueId);

	List<HarvestedRecord> getByDedupRecord(DedupRecord dedupRecord);
	
	List<HarvestedRecord> getByHarvestConfiguration(ImportConfiguration configuration);

	List<HarvestedRecord> getByDedupRecordWithDeleted(DedupRecord dedupRecord);

	boolean existsByDedupRecord(DedupRecord dedupRecord);

	HarvestedRecord findByRecordId(String uniqueId);
	
	void dropDedupKeys(HarvestedRecord hr);

	void dropBilioLinkerKeys(HarvestedRecord hr);

	void dropOtherKeys(HarvestedRecord hr);

	void updateTimestampOnly(HarvestedRecord hr);

	boolean existsUpvApplicationId(String applId);

	void deleteUpvApplicationRecord(String appId);

	String getIdBySigla(String sigla);

	String getRecordIdBy001(Long ConfigurationId, String id);

	Collection<HarvestedRecord> getByBiblioLinkerAndNotDedupRecord(Collection<DedupRecord> drs, Collection<BiblioLinker> bls);

	Collection<HarvestedRecord> getByBiblioLinkerId(Long blId);

	Collection<HarvestedRecord> getByBiblioLinkerIds(List<Long> blId);

	Collection<HarvestedRecord> getByBiblioLinkerIdAndSimilarFlag(Long blId);
}
