import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.IndexingFacade;

public class ReindexingScript implements Runnable {

	@Autowired
	private IndexingFacade indexingFacade;

	@Override
	public void run() {
		indexingFacade.reindex();
	}

}
