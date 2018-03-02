package cz.mzk.recordmanager.server.facade;

import java.time.LocalDateTime;

public interface IndexingFacade {

	LocalDateTime getLastIndexed(String solrUrl);

	void index();

	void index(String solrUrl);

	LocalDateTime getLastReindexed(String solrUrl);

	void reindex();

	void reindex(String solrUrl);

	LocalDateTime getLastIndexedHarvestedRecords(String solrUrl);

	void indexHarvestedRecords();

	void indexHarvestedRecords(String solrUrl);

	LocalDateTime getLastReindexedHarvestedRecords(String solrUrl);

	void reindexHarvestedRecords(String solrUrl);

	void reindexHarvestedRecords();

	void indexIndividualRecordsToSolrJob(String id);

}
