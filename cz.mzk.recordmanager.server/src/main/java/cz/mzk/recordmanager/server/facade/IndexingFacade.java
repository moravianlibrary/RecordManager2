package cz.mzk.recordmanager.server.facade;

import java.util.Date;

public interface IndexingFacade {

	public void index();

	public void index(String solrUrl);

	public Date getLastIndexed(String solrUrl);

	public void indexHarvestedRecords();

	public void indexHarvestedRecords(String solrUrl);

	public Date getLastIndexedHarvestedRecords(String solrUrl);

}
