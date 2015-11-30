package cz.mzk.recordmanager.server.facade;

import java.util.Date;

public interface IndexingFacade {

	public void index(String solrUrl);

	public Date getLastIndexed(String solrUrl);

}
