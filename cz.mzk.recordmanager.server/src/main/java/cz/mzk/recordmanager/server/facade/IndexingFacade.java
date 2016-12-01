package cz.mzk.recordmanager.server.facade;

import java.time.LocalDateTime;

public interface IndexingFacade {

	public LocalDateTime getLastIndexed(String solrUrl);

	public void index();

	public void index(String solrUrl);

	public LocalDateTime getLastReindexed(String solrUrl);

	public void reindex();

	public void reindex(String solrUrl);

	public LocalDateTime getLastIndexedHarvestedRecords(String solrUrl);

	public void indexHarvestedRecords();

	public void indexHarvestedRecords(String solrUrl);

	public LocalDateTime getLastReindexedHarvestedRecords(String solrUrl);

	public void reindexHarvestedRecords(String solrUrl);

	public void reindexHarvestedRecords();

	public void indexIndividualRecordsToSolrJob(String id);

}
